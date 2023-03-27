package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.data.TemperatureSample;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.data.generator.Generator;
import io.bonitoo.qa.data.generator.Utils;

public class MqttTemperatureAnon extends Device {

    MqttClientBlocking client;

    private MqttTemperatureAnon(){
        super();
    }

    static public MqttTemperatureAnon Device(MqttClientBlocking client, DeviceConfig config){
        MqttTemperatureAnon mqtp = new MqttTemperatureAnon();
        mqtp.client = client;
        mqtp.config = config;
        return mqtp;
    }

    @Override
    public void run() {

        long ttl = System.currentTimeMillis() + Config.ttl();

        try {
            client.connect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            while(System.currentTimeMillis() < ttl){
                client.publish(config.getSamples().get(0).getTopic(), Utils.pojoToJSON(new TemperatureSample(config.getId(),
                        System.currentTimeMillis(),
                        Generator.genTemperature(System.currentTimeMillis()))));
                Thread.sleep(config.getInterval());
            }
        } catch (JsonProcessingException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            client.disconnect();
        }


    }
}
