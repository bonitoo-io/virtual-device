package io.bonitoo.qa.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.data.*;
import io.bonitoo.qa.data.generator.DataGenerator;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.data.generator.SimpleStringGenerator;
import io.bonitoo.qa.data.serializer.ItemSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

  // TODO handle precision in serializer and per instance
  //static double precision = 1e3;
  Object val;

  ItemConfig config;

  String label;

  DataGenerator generator;

  public Item(ItemConfig config, Object init) {
    this.val = init;
    this.config = config;
    this.label = config.getLabel();
    this.generator = null;
  }

  public Item(ItemConfig config, Object init, DataGenerator generator) {
    this.val = init;
    this.config = config;
    this.label = config.getLabel();
    this.generator = generator;
  }


 // public static double setPrecision(double prec) {
 //   precision = prec;
 //   return precision;
 // }

 // public static double getPrecision() {
 //   return precision;
 // }

  public ItemType getType() {
    return config.getType();
  }

  /**
   * Generates a new Item instance based on an ItemConfig.
   *
   * @param config - the ItemConfig.
   * @return - an Item instance.
   */

  /* OLD version
  public static Item of(ItemConfig config) {

    Item it;

    switch (config.getType()) {
      case BuiltInTemp:
        it = new Item(NumGenerator.genTemperature(System.currentTimeMillis()), config,"temp", null);
        break;
      case Double:
        it = new Item(NumGenerator.precision(NumGenerator.genDoubleVal(
          ((ItemNumConfig) config).getPeriod(),
          ((ItemNumConfig) config).getMin(),
          ((ItemNumConfig) config).getMax(),
          System.currentTimeMillis()), precision), config, config.getName(), null);
        break;
      case Long:
        it = new Item((long) NumGenerator.genDoubleVal(
          ((ItemNumConfig) config).getPeriod(),
          ((ItemNumConfig) config).getMin(),
          ((ItemNumConfig) config).getMax(),
          System.currentTimeMillis()), config, config.getName(), null);
        break;
      case String:
        List<String> values = ((ItemStringConfig) config).getValues();
        String value = values.get((int) Math.floor(Math.random() * values.size()));
        it = new Item(value, config, config.getName(), null);
        break;
      case Plugin:
     //   ItemGenPlugin generator = ((ItemPluginConfig) config).getItemGen();
        ItemGenPlugin generator = (ItemGenPlugin) DataGenerator.create(config.getGenClassName());
        // todo check for extra params that can be sent to genData
        it = new Item(generator.genData(), config, config.getName(), null);
        break;
      default:
        throw new RuntimeException("Unsupported Type " + config.getType());
    }

    return it;
  } */

  public static Item of(ItemConfig config) {
    Item it;
    switch (config.getType()) {
      case BuiltInTemp:
        NumGenerator builtInNg = (NumGenerator) DataGenerator.create(NumGenerator.class.getName());
        it = new Item(config, 0.0, builtInNg);
        it.update("genTemperature", System.currentTimeMillis());
        break;
      case Double:
      case Long:
        NumGenerator ng = (NumGenerator) DataGenerator.create(config.getGenClassName());
        it = new Item(config, 0.0, ng);
        it.update(
            ((ItemNumConfig) config).getPeriod(),
            ((ItemNumConfig) config).getMin(),
            ((ItemNumConfig) config).getMax(),
            System.currentTimeMillis()
        );
        break;
      case String:
        SimpleStringGenerator sg =
            (SimpleStringGenerator) DataGenerator.create(config.getGenClassName());
        it = new Item(config, "", sg);
        it.update(((ItemStringConfig) config).getValues());
        break;
      case Plugin:
        // ItemGenPlugin plugGen = ((ItemPluginConfig) config).getItemGen();
        DataGenerator<? extends DataConfig> dg = DataGenerator.create(config.getGenClassName());
        it = new Item(config, null, dg);
        it.update();
        break;
      default:
        throw new VirDevConfigException(String.format("Unknown Item Type: %s",
          config.getType()));

    }
    return it;
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

  public Item update() {

    List<Object> args = new ArrayList<>();
    for (String arg : this.getConfig().getUpdateArgs()) {
      if (! arg.equalsIgnoreCase("time")) {
        try {
          Object obj = ItemConfig.getFieldVal(this.getConfig(), arg);
          // expand container args directly to method args
          if (obj instanceof Collection) {
            args.addAll((Collection<?>) obj);
          } else {
            args.add(ItemConfig.getFieldVal(this.getConfig(), arg));
          }
        } catch (NoSuchFieldException | IllegalAccessException e) {
          throw new VirDevConfigException(
            String.format("Mismatched or missing argument \"%s\" for update method.\n%s", arg, e)
          );
        }
      } else {
        args.add(System.currentTimeMillis());
      }
    }

    update(args);
    return this;
 }

  public Item update(List<?> args) {
    return update(args.toArray());
  }

  public Item update(Object... args) {
    Object obj = generator.genData(args);
    switch (config.getType()) {
      case Long:
        val = ensureLong(obj);
        break;
      case Double:
      case BuiltInTemp:
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


}
