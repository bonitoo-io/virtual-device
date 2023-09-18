package io.bonitoo.qa.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Sample holds any number of items the values of which can be randomly generated.
 * It can then be serialized to an MQTT message.  The files timestamp and items are
 * serialized to JSON as the message payload.  The topic field is reserved for the MQTT
 * topic header.
 */
@Setter
@Getter
public abstract class Sample {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public String id;

  @JsonIgnore //should not be part of payload
  public String topic;

  public long timestamp;

  @JsonIgnore
  private SampleConfig config;

  @JsonIgnore
  @JsonAnyGetter
  public Map<String, List<Item>> items;

  public abstract Sample update();

  /**
   * Helper factory method.
   */
  public static Sample of(Function<SampleConfig, Sample> init, SampleConfig config) {
    Sample s = init.apply(config);
    s.setConfig(config);
    return s;
  }

  public Item item(String name) {
    return items.get(name).get(0);
  }

  public Object itemVal(String name) {
    return items.get(name).get(0).getVal();
  }

  protected static Item getItemFromConfig(ItemConfig ic) {
    if (ic instanceof ItemPluginConfig) {
      try {
        return ItemPluginMill.genNewInstance(
          ((ItemPluginConfig) ic).getPluginName(),
          (ItemPluginConfig) ic).getItem();
      } catch (PluginConfigException | NoSuchMethodException | InvocationTargetException
               | InstantiationException | IllegalAccessException e) {
        throw new VirtualDeviceRuntimeException(
          String.format("Failed to generate item plugin %s for config %s",
            ((ItemPluginConfig) ic).getPluginName(), ic.getName()), e);
      }
    }
    return Item.of(ic);
  }

  @Override
  public String toString() {
    checkNameClash();
    StringBuilder result = new StringBuilder(
        String.format("id=%s,timestamp=%d,items=[", id, timestamp)
    );
    for (String key : items.keySet()) {
      if (items.get(key).get(0).getVal() instanceof Double) {
        result.append(String.format("name:%s,val:%.2f,", key, items.get(key).get(0).asDouble()));
      } else if (items.get(key).get(0).getVal() instanceof String) {
        result.append(String.format("name:%s,val:%s,", key, items.get(key)));
      } else {
        result.append(String.format("name:%s,val:%d,", key, items.get(key).get(0).asLong()));
      }
    }
    return result.append("]\n").toString();
  }

  protected void checkNameClash() {

    List<String> toRemove = new ArrayList<>();

    for (String item : items.keySet()) {
      for (Field f : Sample.class.getDeclaredFields()) {
        if (f.getName().equalsIgnoreCase(item)) {
          toRemove.add(item);
        }
      }
    }

    for (String key : toRemove) {
      logger.warn(String
          .format("Item field name %s not allowed, item removed from sample list.",
            key));
      items.remove(key);
    }
  }

  public SampleConfig getConfig() {
    return this.config;
  }

  @JsonIgnore
  public String getName() {
    return this.config.getName();
  }

  /**
   * Serializes the Sample instance to a JSON string.
   *
   * @return - a JSON string.
   * @throws JsonProcessingException - thrown when object cannot be serialized.
   */
  public abstract String toJson() throws JsonProcessingException;
}
