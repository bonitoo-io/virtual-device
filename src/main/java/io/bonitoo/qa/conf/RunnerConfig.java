package io.bonitoo.qa.conf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = RunnerConfigDeserializer.class)
public class RunnerConfig {

    BrokerConfig broker;
    List<DeviceConfig> devices;
    Long ttl;

    @Override
    public String toString(){
        String result = String.format("ttl:%d\nBroker: %s\n", ttl, broker);

        result += "\n";

        for(int i = 0; i < devices.size(); i++){
            result += String.format("Device:%s", devices.get(i));
        }

        return result;
    }

     public SampleConfig sampleConf(int ofDev, int index){
        return devices.get(ofDev).getSample(index);
    }
}