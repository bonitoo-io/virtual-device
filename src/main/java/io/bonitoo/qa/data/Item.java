package io.bonitoo.qa.data;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.data.generator.NumGenerator;
import java.util.List;
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
public class Item {

  static double precision = 1e3;
  Object val;

  public static double setPrecision(double prec) {
    precision = prec;
    return precision;
  }

  public static double getPrecision() {
    return precision;
  }

  /**
   * Generates a new Item instance based on an ItemConfig.
   *
   * @param config - the ItemConfig.
   * @return - an Item instance.
   */

  public static Item of(ItemConfig config) {

    Item it;

    switch (config.getType()) {
      case BuiltInTemp:
        it = new Item(NumGenerator.genTemperature(System.currentTimeMillis()));
        break;
      case Double:
        it = new Item(NumGenerator.precision(NumGenerator.genDoubleVal(
          ((ItemNumConfig) config).getPeriod(),
          ((ItemNumConfig) config).getMin(),
          ((ItemNumConfig) config).getMax(),
          System.currentTimeMillis()), precision));
        break;
      case Long:
        it = new Item((long) NumGenerator.genDoubleVal(
          ((ItemNumConfig) config).getPeriod(),
          ((ItemNumConfig) config).getMin(),
          ((ItemNumConfig) config).getMax(),
          System.currentTimeMillis()));
        break;
      case String:
        List<String> values = ((ItemStringConfig) config).getValues();
        String value = values.get((int) Math.floor(Math.random() * values.size()));
        it = new Item(value);
        break;
      default:
        throw new RuntimeException("Unsupported Type " + config.getType());
    }

    return it;
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
