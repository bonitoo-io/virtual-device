package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.data.ItemType;
import java.lang.reflect.Field;
import java.util.Vector;
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

  protected Vector<String> updateArgs;

  /**
   * Base constructor.
   *
   * @param name - name handle for the item.
   * @param label - label to be used when serializing the item to JSON.
   * @param type - type to which the item will conform.
   * @param genClassName - class name of the data generator to be instantiated.
   * @param updateArgs - names of the argument fields in the child configuration to be passed to
   *         the generator <code>genData</code> method.
   *
   */
  public ItemConfig(String name,
                    String label,
                    ItemType type,
                    String genClassName,
                    Vector<String> updateArgs) {
    this.name = name;
    this.type = type;
    this.label = label;
    this.genClassName = genClassName;
    this.updateArgs = updateArgs;
    ItemConfigRegistry.add(this.name, this);
  }

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
    this.updateArgs = new Vector<>();
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
    this.updateArgs = new Vector<>();
    for (String s : orig.getUpdateArgs()) {
      this.updateArgs.add(String.copyValueOf(s.toCharArray()));
    }
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
    return String.format("name:%s,label:%s,type:%s,className:%s,updateArgs:%s",
      name, label, type, genClassName, updateArgs);
  }

  public ItemConfig copy() {

    return new ItemConfig(this.name, this.label, this.type, this.genClassName, this.updateArgs);
  }

}
