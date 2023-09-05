package io.bonitoo.qa.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.data.ItemArType;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.serializer.GenericSampleSerializer;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A configurable sample based on a SampleConfig.
 */
@JsonSerialize(using = GenericSampleSerializer.class)
public class GenericSample extends Sample {

  private static Item getItemFromConfig(ItemConfig ic) {
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

  /**
   * A method for generating a sample based on a SampleConfig.
   *
   * @param conf - the config
   * @return - A genericSample that can be serialized to an MQTT message
   */
  public static GenericSample of(SampleConfig conf) {
    GenericSample gs = new GenericSample();
    gs.id = conf.getId();
    gs.topic = conf.getTopic();
    gs.items = new HashMap<>();

    for (ItemConfig ic : conf.getItems()) {
      if (ic.getCount() < 1) {
        throw new VirtualDeviceRuntimeException(
          String.format("Encountered ItemConfig %s with count less than 1. Count is %d.",
            ic.getName(), ic.getCount())
        );
      }

      // Sync any undefined arrayTypes with arrayType for sample
      if (ic.getArType() == ItemArType.Undefined
          && conf.getArType() != ItemArType.Undefined) {
        ic.setArType(conf.getArType());
      }

      gs.getItems().put(ic.getName(), new ArrayList<>());

      for (int i = 0; i < ic.getCount(); i++) {
        gs.getItems().get(ic.getName()).add(getItemFromConfig(ic));
      }

    }

    gs.timestamp = System.currentTimeMillis();

    return gs;
  }

  @Override
  public GenericSample update() {
    for (String itemName : items.keySet()) {
      for (Item item : items.get(itemName)) {
        item.update();
      }
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

    ObjectMapper om = new ObjectMapper();

    return om.writer().withDefaultPrettyPrinter().writeValueAsString(this);

  }
}
