package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.data.TemperatureSample;
import io.bonitoo.qa.mqtt.MqttClientBlocking;
import io.bonitoo.qa.util.Config;
import io.bonitoo.qa.util.Generator;
import io.bonitoo.qa.util.Utils;

public class MqttTemperatureAnon extends AbstractDevice{

    MqttClientBlocking client;

    private MqttTemperatureAnon(){
        super();
    }

    static public MqttTemperatureAnon Device(MqttClientBlocking client, String topic){
        MqttTemperatureAnon mqtp = new MqttTemperatureAnon();
        mqtp.client = client;
        mqtp.topic = topic;
        mqtp.id = Config.getDeviceID();
        mqtp.name = Config.getProp("device.name") == null ?
                Class.class.getName() :
                Config.getProp("device.name");
        mqtp.description = Config.getProp("device.description") == null ?
                "Anonymous temperature reporting device" :
                Config.getProp("device.description");
        mqtp.interval = Long.parseLong(Config.getProp("device.interval"));
        return mqtp;
    }

    @Override
    public void run() {

        long ttl = System.currentTimeMillis() + Long.parseLong(Config.getProp("device.ttl"));

        try {
            client.connectAnon();
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
