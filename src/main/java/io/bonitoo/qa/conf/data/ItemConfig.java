package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.data.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration to be passed when generating an item instance.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = ItemConfigDeserializer.class)
public class ItemConfig extends DataConfig {

  //String name;

  ItemType type;

  /**
   * Base constructor.
   *
   * @param name - name handle for the item.
   * @param type - type to which the item will conform.
   */
  public ItemConfig(String name, ItemType type) {
    this.name = name;
    this.type = type;
    ItemConfigRegistry.add(this.name, this);
  }

  /**
   * Returns an ItemConfig instance from the ItemConfigRegistry.
   *
   * @param name - name of the itemConfig to be retrieved.
   * @return - the desired ItemConfig.
   */
  public static ItemConfig get(String name) {
    return ItemConfigRegistry.get(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof ItemConfig)) {
      return false;
    }

    final ItemConfig conf = (ItemConfig) obj;

    return (name.equals(conf.name) && type.equals(conf.type));
  }

  @Override
  public String toString() {
    return String.format("name:%s,type:%s", name, type);
  }

}
