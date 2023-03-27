package io.bonitoo.qa.conf.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores ItemConfigs for later retrieval.
 *
 * <p>N.B. in future this might be better handled in a database.
 */
public class ItemConfigRegistry {

  static Map<String, ItemConfig> registry = new HashMap<>();

  public static void add(String key, ItemConfig item) {
    registry.put(key, item);
  }

  /**
   * Retrieves an ItemConfig instance by name.
   *
   * @param key - name of the ItemConfig instance to be retrieved.
   * @return - the ItemConfig instance.
   */
  public static ItemConfig get(String key) {

    ItemConfig item = registry.get(key);

    if (item == null) {
      throw new RuntimeException(String.format("Item Configuration named %s not found", key));
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
