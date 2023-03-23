package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.util.LogHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenericDevice extends Device{

    int number;

    MqttClientBlocking client;

    static public GenericDevice SingleDevice(MqttClientBlocking client, DeviceConfig config){
        return NumberedDevice(client, config, 1);
    }

    static public GenericDevice NumberedDevice(MqttClientBlocking client, DeviceConfig config, int number){
        GenericDevice device = new GenericDevice();
        device.client = client;
        device.config = config;
        device.number = number;
        return device;
    }

    @Override
    public void run(){

        long ttl = System.currentTimeMillis() + Config.TTL();


        try {
            if(config.getJitter() > 0) {
                Thread.sleep(config.getJitter() * number);
            }
            logger.info(LogHelper.buildMsg(config.getId(), "Device Connection", ""));
            client.connect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            while(System.currentTimeMillis() < ttl){
                logger.debug(LogHelper.buildMsg(config.getId(),"Wait to publish", Long.toString((ttl - System.currentTimeMillis()))));
                Thread.sleep(config.getJitter());
                for(SampleConfig sampleConf : config.getSamples()){
                    String jsonSample = GenericSample.of(sampleConf).toJson();
                    logger.debug(LogHelper.buildMsg(config.getId(),"Publishing", jsonSample));
                    client.publish(sampleConf.getTopic(),
                            jsonSample);

                }
                Thread.sleep(config.getInterval());
            }
            logger.debug(LogHelper.buildMsg(config.getId(),"Published", Long.toString((ttl - System.currentTimeMillis()))));
        } catch (JsonProcessingException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally{
            client.disconnect();
        }

    }
}
