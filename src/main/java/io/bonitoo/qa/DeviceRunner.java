package io.bonitoo.qa;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.device.MqttTemperatureAnon;
import io.bonitoo.qa.mqtt.MqttClientBlocking;
import io.bonitoo.qa.util.Config;

public class DeviceRunner {

    static public void main(String[] args) throws JsonProcessingException {

        MqttClientBlocking client = MqttClientBlocking.Client();
        MqttTemperatureAnon device = MqttTemperatureAnon.Device(client, Config.getProp("publish.topic"));
        device.run();

    }


}
