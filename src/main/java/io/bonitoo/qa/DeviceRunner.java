package io.bonitoo.qa;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.device.Device;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.device.GenericDevice;
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

        List<DeviceConfig> devConfigs = Config.getDeviceConfs();

        BrokerConfig broker = Config.getBrokerConf();

        for(DeviceConfig devConf : devConfigs) {
            for(int i = 0; i < devConf.getCount(); i++) {
                if(devConf.getCount() > 1){
                    DeviceConfig copyDevConfig = new DeviceConfig(devConf, (i+1));
                    devices.add(GenericDevice.NumberedDevice(MqttClientBlocking.Client(broker, copyDevConfig.getId()), copyDevConfig, (i+1)));
                }else{
                    devices.add(GenericDevice.SingleDevice(MqttClientBlocking.Client(broker, devConf.getId()), devConf));
                }
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(devices.size());

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
