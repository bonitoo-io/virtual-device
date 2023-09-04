package io.bonitoo.qa.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.data.ItemArType;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.serializer.GenericSampleSerializer;
import io.bonitoo.qa.data.serializer.GenericSampleSerializerOrig;
import io.bonitoo.qa.data.serializer.ItemSerializer;
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
   * @param config - the config
   * @return - A genericSample that can be serialized to an MQTT message
   */
  /*
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
  }*/

  /*

  public static GenericSample of(SampleConfig config) {

    GenericSample gs = new GenericSample();
    gs.id = config.getId();
    gs.topic = config.getTopic();
    gs.items = new HashMap<>();
  //  gs.dItems = new HashMap<>();

    for (ItemConfig ic : config.getItems()) {
//      System.out.println("DEBUG ic " + ic);
//      System.out.println("DEBUG processing item");
   //   Item baseItem = getItemFromConfig(ic);
  //    System.out.println("DEBUG baseItem " + baseItem);
      if (ic.getCount() < 1) {
        throw new VirtualDeviceRuntimeException(
          String.format("Encountered ItemConfig %s with count less than 1. Count is %d.",
            ic.getName(), ic.getCount())
        );
      }
      if (ic.getCount() == 1) {
        gs.getItems().put(ic.getName(), getItemFromConfig(ic));
      } else { // have array
        String labelFormat = "%s%0" + (((int) Math.ceil(Math.log10(ic.getCount()))) + 1) + "d";
//        System.out.println("DEBUG labelFormat " + labelFormat);
        for (int i = 0; i < ic.getCount(); i++) {
          gs.getItems().put(ic.getName() + (i + 1), getItemFromConfig(ic));
          String curLabel = gs.getItems().get(ic.getName() + (i + 1)).getLabel();
//          System.out.println("DEBUG curLabel: " + curLabel);
          gs.getItems().get(ic.getName() + (i + 1)).setLabel(
              String.format(labelFormat, curLabel, (i + 1))
          );
        }
      }
    }
    gs.timestamp = System.currentTimeMillis();

    return gs;
  }

   */

  public static GenericSample of(SampleConfig conf){
    GenericSample gs = new GenericSample();
    gs.id = conf.getId();
    gs.topic = conf.getTopic();
    gs.items = new HashMap<>();
 //   gs.dItems = new HashMap<>();

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

  /*
  @Override
  public GenericSample update() {
    for (String itemName : items.keySet()) {
      items.get(itemName).update();
    }
    this.timestamp = System.currentTimeMillis();
    return this;
  }

   */

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


  public String toJsonOrig() throws JsonProcessingException {
    checkNameClash();
    // todo add pretty print option.
    ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return objectWriter.writeValueAsString(this);
  }


  public String toJson() throws JsonProcessingException {

    checkNameClash();

    ObjectMapper om = new ObjectMapper();
 //   SimpleModule sMod = new SimpleModule("GenericSampleExperimSerializer", new Version(0,1,0,null,null,null));
 //   om.disable(MapperFeature.USE_ANNOTATIONS);

//    sMod.addSerializer(GenericSample.class, new GenericSampleSerializer());
//        sMod.addSerializer(Item.class, new ItemSerializer());
//    om.registerModule(sMod);
    for (Object modId : om.getRegisteredModuleIds()) {
      System.out.println("DEBUG modID " + modId);
    }

    return om.writer().withDefaultPrettyPrinter().writeValueAsString(this);

  }
}
