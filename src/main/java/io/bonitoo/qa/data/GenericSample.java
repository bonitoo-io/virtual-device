package io.bonitoo.qa.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.serializer.GenericSampleSerializer;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * A configurable sample based on a SampleConfig.
 */
@JsonSerialize(using = GenericSampleSerializer.class)
public class GenericSample extends Sample {

  /**
   * A method for generating a sample based on a SampleConfig.
   *
   * @param config - the config
   * @return - A genericSample that can be serialized to an MQTT message
   */
  public static GenericSample of(SampleConfig config) {
    GenericSample gs = new GenericSample();
    gs.id = config.getId();
    gs.topic = config.getTopic();
    gs.items = new HashMap<>();
    for (ItemConfig itemConfig : config.getItems()) {
      if (itemConfig instanceof ItemPluginConfig) {
        try {
          gs.getItems().put(itemConfig.getName(), ItemPluginMill.genNewInstance(
              ((ItemPluginConfig) itemConfig).getPluginName(),
              (ItemPluginConfig) itemConfig).getItem());
        } catch (PluginConfigException | NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      } else {
        gs.getItems().put(itemConfig.getName(), Item.of(itemConfig));
      }
    }
    gs.timestamp = System.currentTimeMillis();

    return gs;
  }

  @Override
  public GenericSample update() {
    for (String itemName : items.keySet()) {
      items.get(itemName).update();
    }
    this.timestamp = System.currentTimeMillis();
    return this;
  }

  /**
   * Serialize the sample to JSON.
   *
   * @return - a JSON representation of the object.
   * @throws JsonProcessingException - when object cannot be serialized.
   */

  public String toJson() throws JsonProcessingException {
    checkNameClash();
    // todo add pretty print option.
    ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return objectWriter.writeValueAsString(this);
  }
}
