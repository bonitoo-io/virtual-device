package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.conf.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenericDevice extends Device{

    MqttClientBlocking client;

    static public GenericDevice Device(MqttClientBlocking client, DeviceConfig config){
        GenericDevice device = new GenericDevice();
        device.client = client;
        device.config = config;
        return device;
    }

    @Override
    public void run(){

        long ttl = System.currentTimeMillis() + Config.TTL();


        try {
            if(config.getJitter() > 0) {
                Thread.sleep(config.getJitter());
            }
            System.out.println("GENERIC CONNECTING");
            client.connect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            while(System.currentTimeMillis() < ttl){
                System.out.println("GENERIC LOOP " + (ttl - System.currentTimeMillis()));
                Thread.sleep(config.getJitter());
                for(SampleConfig sampleConf : config.getSamples()){
                    String jsonSample = GenericSample.of(sampleConf).toJson();
                    System.out.println("GENERIC PUBLISHING from " + sampleConf + " toJson " + jsonSample);
                    client.publish(sampleConf.getTopic(),
                            jsonSample);

                }
                Thread.sleep(config.getInterval());
            }

            System.out.println("GENERIC LOOP END " + (ttl - System.currentTimeMillis()));
        } catch (JsonProcessingException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally{
            client.disconnect();
        }

    }
}
