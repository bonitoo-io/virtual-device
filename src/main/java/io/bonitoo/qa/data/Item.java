package io.bonitoo.qa.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.conf.data.ItemArType;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.data.generator.DataGenerator;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.data.generator.SimpleStringGenerator;
import io.bonitoo.qa.data.serializer.ItemSerializer;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.item.DataGenPlugin;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import java.lang.reflect.InvocationTargetException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An item to be included in a Sample.  Item instances are configured from an ItemConfig.
 *
 * <p>An item simply wraps an Object identified by the field val.  The actual type of the val field
 * is specified in the ItemConfig upon which the item is based.
 */
@Getter
@AllArgsConstructor
@JsonSerialize(using = ItemSerializer.class)
public class Item {

  Object val;

  ItemConfig config;

  String label;

  DataGenerator<? extends DataConfig> generator;

  /**
   * Basic constructor.
   *
   * <p>Note this leaves the data generator null.
   * It needs to be set before <code>update()</code> is called.</p>
   *
   * @param config - configuration for the item.
   * @param init - initial value.
   */
  public Item(ItemConfig config, Object init) {
    this.val = init;
    this.config = config;
    this.label = config.getLabel();
    this.generator = null;
  }

  /**
   * Base constructor that assigns a DataGenerator to the item.
   *
   * @param config - the config.
   * @param init - an initial value.
   * @param generator - the DataGenerator.
   */
  public Item(ItemConfig config, Object init, DataGenerator<? extends DataConfig> generator) {
    this(config, init);
    this.generator = generator;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public ItemType getType() {
    return config.getType();
  }

  public ItemArType getArType() {
    return config.getArType();
  }

  /**
   * Generates a new Item based on the ItemConfig.
   *
   * @param config - the config.
   * @return - an Item.
   */
  public static Item of(ItemConfig config) {
    Item it;
    switch (config.getType()) {
      case Double:
      case Long:
        NumGenerator ng = (NumGenerator) DataGenerator.create(config.getGenClassName());
        // ensure each new item has its own copy of config so changes impact only this item
        it = new Item(new ItemNumConfig((ItemNumConfig) config), 0.0, ng);
        ng.setItem(it);
        it.update();
        break;
      case String:
        SimpleStringGenerator sg =
            (SimpleStringGenerator) DataGenerator.create(config.getGenClassName());
        // ensure each new item has its own copy of config so changes impact only this item
        it = new Item(new ItemStringConfig((ItemStringConfig) config), "", sg);
        sg.setItem(it);
        it.update();
        break;
      case Plugin:
        // DataGenerator<? extends DataConfig> dg = DataGenerator.create(config.getGenClassName());
        if (! (config instanceof ItemPluginConfig)) {
          throw new VirtualDeviceRuntimeException(
            String.format("Configuration %s is not of type ItemPluginConfig, "
              + "cannot create a plugin from it.", config.getName()));
        }
        PluginProperties props = ItemPluginMill.getPluginProps(((ItemPluginConfig) config)
            .getPluginName());
        if (props == null) {
          throw new VirtualDeviceRuntimeException(
            String.format("Cannot locate %s plugin in ItemPluginMill",
            ((ItemPluginConfig) config).getPluginName())
          );
        }
        it = of(config, props);
        break;
      default:
        throw new VirDevConfigException(String.format("Unknown Item Type: %s",
          config.getType()));
    }
    return it;
  }

  /**
   * Factory method for creating items directly coupled with ItemGenPlugin subtypes.
   *
   * @param config - item configuration.
   * @param props - plugin properties.
   * @return - an item coupled with the plugin.
   */
  public static Item of(ItemConfig config, PluginProperties props) {
    if (config.getType() != ItemType.Plugin) {
      throw new VirDevConfigException("Factory method reserved only for Plugin Types");
    }

    DataGenPlugin<? extends DataConfig> dgp = (DataGenPlugin<? extends DataConfig>)
        DataGenerator.create(config.getGenClassName());

    try {
      @SuppressWarnings("unchecked")
      Item it = new Item(ItemPluginConfig.copy((ItemPluginConfig) config), null, dgp);
      dgp.setItem(it);
      dgp.setProps(props);

      // TODO find way to set initial value
      if (it.val != null) {
        it.update();
      }
      return it;
    } catch (NoSuchMethodException | InvocationTargetException
             | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

  }

  private static Long ensureLong(Object obj) {
    if (obj instanceof Long) {
      return (long) obj;
    } else if (obj instanceof Double) {
      return (long) ((Double) obj).doubleValue();
    } else if (obj instanceof String) {
      return Long.parseLong(obj.toString());
    }
    throw new VirDevConfigException("Cannot get Long from unknown type " + obj);
  }

  private static Double ensureDouble(Object obj) {
    if (obj instanceof Long) {
      return (double) (Long) obj;
    } else if (obj instanceof Double) {
      return (double) obj;
    } else if (obj instanceof String) {
      return Double.parseDouble(obj.toString());
    }
    throw new VirDevConfigException("Cannot get Double from unknown type " + obj);

  }

  /**
   * Updates the internal value of the item.
   *
   * @return - the updated item.
   */
  public Item update() {
    try {
      Object obj = generator.genData();
      switch (config.getType()) {
        case Long:
          val = ensureLong(obj);
          break;
        case Double:
          val = ensureDouble(obj);
          break;
        case String:
          val = obj.toString();
          break;
        case Plugin:
          switch (((ItemPluginConfig) config).getResultType()) {
            case Long:
              val = ensureLong(obj);
              break;
            case Double:
              val = ensureDouble(obj);
              break;
            case String:
              val = obj.toString();
              break;
            default:
              throw new VirDevConfigException("Unknown plugin result type "
                + ((ItemPluginConfig) config).getResultType());
          }
          break;
        default:
          throw new VirDevConfigException("Unknown type " + config.getType());
      }
      return this;
    } catch (Exception e) {
      throw new VirtualDeviceRuntimeException(
         String.format("Failed to execute genData() for generator %s ",
           generator.getClass().getName()), e
       );
    }
  }

  /**
   * Gets the value of the item as type Double.
   *
   * @return - the value as a Double or null if the value is not an instance of Double.
   */
  public Double asDouble() {
    if (val instanceof Double) {
      return (Double) val;
    }
    return null;
  }

  /**
   * Gets the value of the item as type Long.
   *
   * @return - the value as a Long or null if the value is not an instance of Long.
   */
  public Long asLong() {
    if (val instanceof Long) {
      return (Long) val;
    }
    return null;
  }

  /**
   * Gets the value of the item as type String.
   *
   * @return - the value as String or null if the value is not an instance of String.
   */
  public String asString() {
    if (val instanceof String) {
      return (String) val;
    }
    return null;
  }

  /**
   * Helper method for establishing the decimal precision of the value of a Double item.
   *
   * <p>Intended to be used primarily during serialization.</p>
   *
   * @param val - the value to be modified.
   * @param prec - the decimal point precision to be respected.
   * @return - a double truncated to the desired precision.
   */
  public static double precision(double val, int prec) {
    double p;
    if (prec < 0) {  // treat negative values as positive.
      p = Double.parseDouble("1e" + prec) * -1;
    } else {
      p = Double.parseDouble("1e" + prec);
    }
    return (long) (val * p) / p;
  }

  public void setVal(Object obj) {
    val = obj;
  }

}
