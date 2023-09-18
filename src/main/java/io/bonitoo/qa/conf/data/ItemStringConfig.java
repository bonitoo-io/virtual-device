package io.bonitoo.qa.conf.data;

import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.generator.SimpleStringGenerator;
import java.util.ArrayList;
import java.util.List;

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
    super(name, label, type, SimpleStringGenerator.class.getName());

    this.values = values;
    ItemConfigRegistry.add(this.name, this);
  }

  /**
   * Copy constructor used primarily in coupling a config to a new item instance.
   *
   * @param orig - the original config.
   */
  public ItemStringConfig(ItemStringConfig orig) {
    super(orig);
    this.values = new ArrayList<>();
    this.values.addAll(orig.getValues());
  }

  @Override
  public String toString() {
    return String.format("name:%s,label:%s,type:%s,values:%s",
      name, label, type, values);
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
