package io.bonitoo.qa;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.device.Device;
import io.bonitoo.qa.device.MqttTemperatureAnon;
import io.bonitoo.qa.device.MqttTemperatureSimple;
import io.bonitoo.qa.mqtt.MqttClientBlocking;
import io.bonitoo.qa.util.Config;

public class DeviceRunner {

    static public void main(String[] args) throws JsonProcessingException {

        Device device;

        MqttClientBlocking client = MqttClientBlocking.Client();
        if(Config.getProp("device.username") == null){
            System.out.println("Using device with anonymous connection");
            device = MqttTemperatureAnon.Device(client, Config.getProp("publish.topic"));
        }else {
            System.out.println("Using device with simple auth for user " + Config.getProp("device.username"));
            device = MqttTemperatureSimple.Device(client, Config.getProp("publish.topic"));
        }
        device.run();

    }


}
