package io.bonitoo.qa.conf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configures the runner including collecting devices.
 */
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
  public String toString() {
    StringBuilder result = new StringBuilder(String.format("ttl:%d\nBroker: %s\n", ttl, broker));

    result.append("\n");

    for (DeviceConfig device : devices) {
      result.append(String.format("Device:%s", device));
    }

    return result.toString();
  }

  public SampleConfig sampleConf(int ofDev, int index) {
    return devices.get(ofDev).getSample(index);
  }
}