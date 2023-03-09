package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.data.TemperatureSample;
import io.bonitoo.qa.mqtt.MqttClientBlocking;
import io.bonitoo.qa.util.Config;
import io.bonitoo.qa.util.Generator;
import io.bonitoo.qa.util.Utils;

public class MqttTemperatureSimple extends AbstractDevice{


    MqttClientBlocking client;

//    String topic;

//    String id;

//    long interval;

    private MqttTemperatureSimple(){
        super();
    }

    static public MqttTemperatureSimple Device(MqttClientBlocking client, String topic){
        MqttTemperatureSimple mqts = new MqttTemperatureSimple();
        mqts.client = client;
        mqts.topic = topic;
        mqts.id = Config.getDeviceID();
        mqts.name = Config.getProp("device.name") == null ?
                Class.class.getName() :
                Config.getProp("device.name");
        mqts.description = Config.getProp("device.description") == null ?
                "Simply authenticated device for reporting temperature" :
                Config.getProp("device.description");

        mqts.interval = Long.parseLong(Config.getProp("device.interval"));
        return mqts;
    }

    @Override
    public void run() {

        long ttl = System.currentTimeMillis() + Long.parseLong(Config.getProp("device.ttl"));

        try {
            client.connect(Config.getProp("device.username"), Config.getProp("device.password"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            while(System.currentTimeMillis() < ttl){
                client.publish(topic, Utils.pojoToJSON(new TemperatureSample(id,
                        System.currentTimeMillis(),
                        Generator.genTemperature(System.currentTimeMillis()))));
                Thread.sleep(interval);
            }
        } catch (JsonProcessingException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        client.disconnect();

    }

}
