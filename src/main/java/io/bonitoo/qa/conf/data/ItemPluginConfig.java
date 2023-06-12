package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.PluginResultType;
import io.bonitoo.qa.plugin.PluginType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.Getter;
import lombok.Setter;

/**
 * A configuration file for Items leveraging Item Generator Plugins.
 *
 * <p>Note that the configuration file contains a reference to the
 * Item Generator Plugin/</p>
 */
@Getter
@Setter
public class ItemPluginConfig extends ItemConfig {

  String pluginName;

  PluginResultType resultType;

  @JsonIgnore
  PluginType pluginType;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  Integer prec; // can be null

  /**
   * Base Constructor.
   *
   * @param props - properties.
   * @param name - name of the configuration.
   */
  public ItemPluginConfig(PluginProperties props, String name) {

    this.type = ItemType.Plugin;
    this.name = name;
    this.label = props.getLabel();
    this.pluginName = props.getName();
    this.resultType = props.getResultType();
    this.genClassName = props.getMain();
    this.pluginType = PluginType.Item;
    this.prec = props.getPrec();
    ItemConfigRegistry.add(this.name, this);
  }

  /**
   * Copy constructor used primarily in coupling a config to a new item instance.
   *
   * @param config - the original config.
   */
  public ItemPluginConfig(ItemPluginConfig config) {
    super(config);
    this.pluginName = config.pluginName;
    this.pluginType = config.pluginType;
    this.resultType = config.resultType;
    this.prec = config.prec;
  }

  /**
   * Copy factory method.  There are cases where an exact copy of the
   * ItemPluginConfig subtype is what will be required.  This method looks
   * for overriden definitions of the copy constructor and uses them instead.
   *
   * @param config - the config to be copied.
   * @param <T> - any class extending ItemPluginConfig.
   * @return - any class extending ItemPluginConfig.
   * @throws NoSuchMethodException -
   * @throws InvocationTargetException -
   * @throws InstantiationException -
   * @throws IllegalAccessException -
   */
  public static <T extends ItemPluginConfig> ItemPluginConfig copy(T config)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
      IllegalAccessException {

    Constructor<? extends ItemPluginConfig> constructor = config.getClass()
        .getDeclaredConstructor(ItemPluginConfig.class);

    return constructor.newInstance(config);

  }

  @Override
  public String toString() {
    return String.format("%s,pluginName:%s,pluginType:%s,resultType:%s,prec:%s",
      super.toString(), pluginName, pluginType, resultType, prec);
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }

    final ItemPluginConfig conf = (ItemPluginConfig) obj;
    return pluginName.equals(conf.getPluginName())
      && resultType == conf.getResultType()
      && pluginType == conf.getPluginType();
  }

}
