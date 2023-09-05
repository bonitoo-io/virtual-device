package io.bonitoo.qa.conf.data;

import io.bonitoo.qa.conf.VirDevConfigException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A registry of SampleConfigs to be reused when configuring Devices.
 */
public class SampleConfigRegistry {

  static Map<String, SampleConfig> registry = new HashMap<>();

  public static void add(String key, SampleConfig sample) {
    registry.put(key, sample);
  }

  /**
   * Get a SampleConfig instance from the registry by name.
   *
   * <p>A runtime exception is thrown if the SampleConfig instance is not found.
   *
   * @param key - name of the SampleConfig desire.
   * @return - a SampleConfig instance
   */
  public static SampleConfig get(String key) {

    SampleConfig sample = registry.get(key);

    if (sample == null) {
      throw new VirDevConfigException(String.format("Sample Configuration named %s not found", key));
    }

    return registry.get(key);
  }

  public static void clear() {
    registry.clear();
  }

  public static Set<String> keys() {
    return registry.keySet();
  }
}
