package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.data.ItemType;
import java.lang.reflect.Field;
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

  String label;

  protected ItemType type;

  protected String genClassName;

  int count = 1;

  ItemArType arType = ItemArType.Undefined;

  /**
   * Constructor wich adds an empty vector for updateArgs.
   *
   * @param name - name of the config.
   * @param label - label used when serializing the item.
   * @param type - type of Item to be configured.
   * @param genClassName - class name of the item generator to be instantiated.
   */
  public ItemConfig(String name,
                    String label,
                    ItemType type,
                    String genClassName) {
    this.name = name;
    this.type = type;
    this.label = label;
    this.genClassName = genClassName;
    ItemConfigRegistry.add(this.name, this);
  }

  /**
   * A copy constructor used mainly when coupling copies of configs to
   * item instances.
   *
   * <p>Note that copies are not added to the internal ItemConfigRegistry.</p>
   *
   * @param orig - the original configuration.
   */
  public ItemConfig(ItemConfig orig) {
    this.name = orig.getName();
    this.label = orig.getLabel();
    this.type = orig.getType();
    this.genClassName = orig.getGenClassName();
    this.count = orig.getCount();
    this.arType = orig.getArType();
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
      && count == conf.count
      && genClassName.equals(conf.genClassName));
  }

  @Override
  public String toString() {
    return String.format("name:%s,label:%s,type:%s,count:%d,className:%s,arType:%s",
      this.name, this.label, this.type, this.count, this.genClassName, this.arType);
  }

}
