package io.bonitoo.qa.conf.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.conf.data.SampleConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration to be used when instantiating a generic device.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = DeviceConfigDeserializer.class)
public class DeviceConfig {

  String id;
  String name;
  String description;
  List<SampleConfig> samples;
  Long interval;
  Long jitter = 0L;
  int count;

  /**
   * Copy constructor needed when creating more than one device.
   *
   * @param deviceConfig - original deviceConfig.
   * @param number - serial number of deviceConfig from set of deviceConfigs
   *               to which this config belongs.
   */
  public DeviceConfig(DeviceConfig deviceConfig, int number) {
    this.id = String.format("%s-%03d", deviceConfig.getId(), number);
    this.name = String.format("%s-%03d", deviceConfig.getName(), number);
    this.description = deviceConfig.getDescription();
    this.interval = deviceConfig.getInterval();
    this.jitter = deviceConfig.getJitter();
    this.count = 1;
    this.samples = new ArrayList<>();
    // deep copy sample configs and modify
    for (SampleConfig sampleConfig : deviceConfig.getSamples()) {
      SampleConfig newConf = new SampleConfig(sampleConfig);
      newConf.setId(String.format("%s-%03d", sampleConfig.getId(), number));
      newConf.setName(String.format("%s-%03d", sampleConfig.getName(), number));
      this.samples.add(newConf);
    }
  }

  /**
   * Gets the device ID and if the current value matches "RANDOM",
   * updates the id to a random UUID value.
   *
   * @return - the ID or and updated random UUID value.
   */
  public String getId() {
    if (id == null || id.equalsIgnoreCase("RANDOM")) {
      id = UUID.randomUUID().toString();
    }
    return id;
  }

  public SampleConfig getSample(int index) {
    return samples.get(index);
  }

  /**
   * Helper method to get a sample config by name.
   *
   * @param name - the desired SampleConfig instance name;
   * @return - SampleConfig or null if no matching SampleConfig instance name is found.
   */
  public SampleConfig getSample(String name) {
    for (SampleConfig sample : samples) {
      if (sample.getName().equals(name)) {
        return sample;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(
        String.format("name=%s,id=%s,description=%s,interval=%d,jitter=%d,count=%d,\nsamples=[\n",
        name, getId(), description, interval, jitter, count)
    );
    for (SampleConfig sample : samples) {
      result.append(String.format("%s", sample));
    }
    result.append("]\n");
    return result.toString();
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null) {
      return false;
    }

    if (!(obj instanceof DeviceConfig)) {
      return false;
    }

    final DeviceConfig conf = (DeviceConfig) obj;

    if (!(conf.id.equals(id)
        && conf.name.equals(name)
        && conf.interval.equals(interval)
        && conf.jitter.equals(jitter)
        && conf.count == count)) {
      return false;
    }

    for (SampleConfig sample : samples) {
      if (!conf.samples.contains(sample)) {
        return false;
      }
    }

    for (SampleConfig sample : conf.samples) {
      if (!samples.contains(sample)) {
        return false;
      }
    }

    return true;
  }
}
