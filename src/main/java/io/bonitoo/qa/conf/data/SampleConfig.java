package io.bonitoo.qa.conf.data;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a SampleConfiguration to be used to create a Sample instance.
 */
//@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = SampleConfigDeserializer.class)
public class SampleConfig extends DataConfig {

  String id;

  String topic;

  List<ItemConfig> items;

  ItemArType arType = ItemArType.Undefined;

  // optional - if set plugin will be used to generated samples
  String plugin;

  /**
   * Basic all arguments constructor.
   *
   * @param id - id of the sample.
   *           Note - the value "random" leads to a random UUID value being generated.
   * @param name - name of the sample - used in the SampleRegistry and in Device configuration.
   * @param topic - MQTT topic under which randomly generated samples will be published.
   * @param items - List of ItemConfigs representing the contents of the sample.
   */
  public SampleConfig(String id, String name, String topic, List<ItemConfig> items, String plugin) {
    this.id = resolveId(id);
    this.plugin = plugin;
    this.name = name;
    this.topic = topic;
    this.items = items;
    SampleConfigRegistry.add(this.name, this);
  }

  /**
   * Constructor - sets plugin value to null.  Intended for use with internal, legacy
   * and non-plugin samples.
   *
   * @param id - id for this sample config.
   * @param name - name for this sample config, so it can be retrieved.
   * @param topic - MQTT topic under which the sample will be published.
   * @param items - items list.
   */

  public SampleConfig(String id, String name, String topic, List<ItemConfig> items) {
    this.id = resolveId(id);
    this.plugin = null;
    this.name = name;
    this.topic = topic;
    this.items = items;
    SampleConfigRegistry.add(this.name, this);
  }

  /**
   *  Basic all argument constructor, but with a String array aliad for item configurations.
   *
   * <p>With this constructor, ItemConfigs will be fetched from the ItemConfigRegistry by name.
   *  So they should be initialized ahead of calling this constructor.
   *
   * @param id - ID of the sample.
   *           Note the value "random" will lead to the generation of a random UUID value.
   * @param name - name of the sample used as a handle in the SampleRegistry.
   * @param topic - MQTT topic under which the generated samples will be published.
   * @param itemNames - names of ItemConfigs, already in the ItemConfigRegistry.
   */
  public SampleConfig(String id, String name, String topic, String[] itemNames, String plugin) {
    this.id = resolveId(id);
    this.plugin = plugin;
    this.name = name;
    this.topic = topic;
    items = new ArrayList<>();
    if (itemNames != null) {
      for (String itemName : itemNames) {
        items.add(ItemConfigRegistry.get(itemName));
      }
    }
    SampleConfigRegistry.add(this.name, this);
  }

  /**
   * Constructor - sets plugin value to null.  Intended for use with internal,
   * legacy or non-plugin samples.
   *
   * @param id - id for the new sample
   * @param name - name for the new sample
   * @param topic - MQTT topic under which this sample will be published.
   * @param itemNames - names for items as a String array.  They should already be loaded
   *                 exist into the ItemConfigRegistry, from where they will be retrieved.
   */
  public SampleConfig(String id, String name, String topic,  String[] itemNames) {
    this.id = resolveId(id);
    this.plugin = null;
    this.name = name;
    this.topic = topic;
    items = new ArrayList<>();
    if (itemNames != null) {
      for (String itemName : itemNames) {
        items.add(ItemConfigRegistry.get(itemName));
      }
    }
    SampleConfigRegistry.add(this.name, this);
  }

  /**
   * SampleConfig copy constructor to be used when creating multiple devices each with its
   * unique SampleConfig based on an original.
   *
   * @param sampleConfig - the Sample Config to copy.
   */
  public SampleConfig(SampleConfig sampleConfig) {
    this.id = sampleConfig.getId();
    this.plugin = sampleConfig.getPlugin();
    this.name = sampleConfig.getName();
    this.topic = sampleConfig.getTopic();
    this.items = sampleConfig.getItems();
    this.arType = sampleConfig.getArType();
  }

  /**
   * Utility method for determining whether a random ID needs to be generated or not.
   *
   * @param id - the original ID from the configuration.
   * @return - either the original ID or a random UUID value.
   */
  public static String resolveId(String id) {
    if (id.equalsIgnoreCase("RANDOM")) {
      return UUID.randomUUID().toString();
    }
    return id;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(
        String.format("id=%s,name=%s,topic=%s,items[\n", id, name, topic)
    );
    if (items != null) {
      for (ItemConfig item : items) {
        result.append(String.format("Item:%s", item));
      }
    }
    result.append("]\n");
    return result.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof SampleConfig)) {
      return false;
    }

    final SampleConfig conf = (SampleConfig) obj;

    if (!(name.equals(conf.name)
        && id.equals(conf.id)
        && topic.equals(conf.topic))) {
      return false;
    }

    for (ItemConfig itemConfig : items) {
      if (!conf.items.contains(itemConfig)) {
        return false;
      }
    }

    for (ItemConfig itemConfig : conf.items) {
      if (!items.contains(itemConfig)) {
        return false;
      }
    }

    return true;
  }

}
