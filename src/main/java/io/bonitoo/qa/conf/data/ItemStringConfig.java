package io.bonitoo.qa.conf.data;

import io.bonitoo.qa.data.ItemType;

import java.util.*;

import io.bonitoo.qa.data.generator.SimpleStringGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration for an Item containing a string value to be pulled from a randomized list.
 *
 * <p>Note - for an invariable string value add only one item to the list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemStringConfig extends ItemConfig {

  List<String> values;

  /**
   * Standard all fields constructor.
   *
   * @param name - name of the item.
   * @param type - type of the item.  Should be ItemType.String.
   * @param values - list of string values to be randomized.
   */
  public ItemStringConfig(String name, String label, ItemType type, List<String> values) {
    super(name, label, type, SimpleStringGenerator.class.getName(),
      new Vector<>(Collections.singletonList("values")));
    this.values = values;
    ItemConfigRegistry.add(this.name, this);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(
        String.format("name:%s,type:%s,values:[", name, type)
    );
    for (String val : values) {
      result.append(String.format("%s,", val));
    }
    result.append("]\n");
    return result.toString();
  }

  @Override
  public boolean equals(Object obj) {

    if (!super.equals(obj)) {
      return false;
    }

    final ItemStringConfig conf = (ItemStringConfig) obj;

    if (values.size() != conf.values.size()) {
      return false;
    }

    for (String s : values) {
      if (!conf.values.contains(s)) {
        return false;
      }
    }

    for (String s : conf.values) {
      if (!values.contains(s)) {
        return false;
      }
    }

    return true;

  }
}
