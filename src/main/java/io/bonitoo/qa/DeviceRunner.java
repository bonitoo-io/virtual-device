package io.bonitoo.qa;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.device.Device;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.device.MqttTemperatureAnon;
import io.bonitoo.qa.device.MqttTemperatureSimple;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.conf.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeviceRunner {

    /*
    TODO Note on configuration - using device.conf should lead to creating only 1 device by default
    using a yml file should lead to creating multiple devices
     */
    static public void main(String[] args) throws JsonProcessingException {

        List<Device> devices = new ArrayList<>();

        ExecutorService service = Executors.newFixedThreadPool(2);

        DeviceConfig config = Config.getDeviceConfs().get(0);

        BrokerConfig broker = Config.getBrokerConf();

        MqttClientBlocking client = MqttClientBlocking.Client(broker);

        if(broker.getAuth().getUsername() == null){
            System.out.println("Using device with anonymous connection");
            devices.add(MqttTemperatureAnon.Device(client, config));
        }else {
            System.out.println("Using device with simple auth for user " + broker.getAuth().getUsername());
            devices.add(MqttTemperatureSimple.Device(client, config));
        }

        devices.forEach(device -> {
            service.execute(device);
        });

        try {
            service.awaitTermination(Config.TTL(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        service.shutdown();

    }

}
