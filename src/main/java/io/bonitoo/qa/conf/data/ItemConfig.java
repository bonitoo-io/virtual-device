package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.data.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Configuration to be passed when generating an item instance.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = ItemConfigDeserializer.class)
public class ItemConfig extends DataConfig {

  String label;

  protected ItemType type;

  protected String genClassName;

  protected Vector<String> updateArgs;

  /**
   * Base constructor.
   *
   * @param name - name handle for the item.
   * @param type - type to which the item will conform.
   */
  public ItemConfig(String name, String label, ItemType type, String genClassName, Vector<String> updateArgs) {
    this.name = name;
    this.type = type;
    this.label = label;
    this.genClassName = genClassName;
    this.updateArgs = updateArgs;
    ItemConfigRegistry.add(this.name, this);
  }

  public ItemConfig(String name, String label, ItemType type, String genClassName) {
    this.name = name;
    this.type = type;
    this.label = label;
    this.genClassName = genClassName;
    this.updateArgs = new Vector<>();
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

  public static Object getFieldVal(ItemConfig conf, String fieldName)
    throws NoSuchFieldException, IllegalAccessException {
    Field field = conf.getClass().getDeclaredField(fieldName);
    return field.get(conf);
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

    return (name.equals(conf.name)
      && type.equals(conf.type)
      && genClassName.equals(conf.genClassName));
  }

  @Override
  public String toString() {
    return String.format("name:%s,label:%s,type:%s,className:%s", name, label, type, genClassName);
  }

}
