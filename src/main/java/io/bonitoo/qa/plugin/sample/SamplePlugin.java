package io.bonitoo.qa.plugin.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.data.ItemArType;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.data.Sample;
import io.bonitoo.qa.plugin.Plugin;
import io.bonitoo.qa.plugin.PluginProperties;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

/**
 * The basis for Sample Plugins.
 */

@Setter
@Getter
public abstract class SamplePlugin extends Sample implements Plugin {

  @JsonIgnore
  protected PluginProperties props;

  @JsonIgnore
  protected boolean enabled = false;

  /**
   * Factory style method for instantiating SamplePlugins.
   *
   * <p>N.B. - experimental.  The SamplePluginMill generator uses the version
   * of this method that takes a <code>Method</code> argument instead of
   * <code>Function</code>.</p>
   *
   * @param init - A lambda style function that returns a new SamplePlugin.
   * @param config - The sample configuration used.
   * @param props - Plugin Properties.
   * @return - The result of the lambda style function.
   */
  public static SamplePlugin of(Function<SamplePluginConfig, SamplePlugin> init, //+
                                SamplePluginConfig config,
                                PluginProperties props) {
    SamplePlugin sp = init.apply(config);
    sp.props = props;
    sp.applyProps(props);
    sp.setConfig(config);
    return sp;
  }

  /**
   * Factory style method for instantiating SamplePlugin.
   *
   * @param init - A static Java reflection method the instantiates a plugin from a config.
   * @param config - the sample config.
   * @param props - the plugin properties.
   * @return - the result of the init method.
   */
  public static SamplePlugin of(Method init, SamplePluginConfig config, PluginProperties props) {
    try {
      SamplePlugin sp = (SamplePlugin) init.invoke(null, config);
      sp.onLoad();
      sp.props = props;
      sp.applyProps(props);
      sp.setConfig(config);
      return sp;
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Constructs a Sample Plugin.
   *
   * @param props - plugin properties.
   * @param config - configuration for the generated sample.
   */
  public SamplePlugin(PluginProperties props, SampleConfig config) {
    this.props = props;
    this.id = config.getId();
    this.topic = config.getTopic();
    this.items = new HashMap<>();
    for (ItemConfig itemConfig : config.getItems()) {
      this.items.put(itemConfig.getName(), new ArrayList<>());
      this.items.get(itemConfig.getName()).add(Item.of(itemConfig));
      // this.items.put(itemConfig.getName(), Item.of(itemConfig));
      if (itemConfig.getCount() < 1) {
        throw new VirtualDeviceRuntimeException(
          String.format("Encountered ItemConfig %s with count less than 1. Count is %d.",
            itemConfig.getName(), itemConfig.getCount())
        );
      }

      // Sync any undefined arrayTypes with arrayType for sample
      if (itemConfig.getArType() == ItemArType.Undefined
          && config.getArType() != ItemArType.Undefined) {
        itemConfig.setArType(config.getArType());
      }

      this.items.put(itemConfig.getName(), new ArrayList<>());
      for (int i = 0; i < itemConfig.getCount(); i++) {
        this.items.get(itemConfig.getName()).add(getItemFromConfig(itemConfig));
      }

    }
    this.timestamp = System.currentTimeMillis();
    this.setConfig(config);
    System.out.println("DEBUG this.config " + this.getConfig());
  }


  @Override
  public void onLoad() {
    enabled = true;
  }

  public boolean onEnable() {
    return enabled = true;
  }

  public boolean onDisable() {
    return enabled = false;
  }

  @Override
  public Sample update() {
    this.timestamp = System.currentTimeMillis();
    return this;
  }

  @Override
  public abstract String toJson() throws JsonProcessingException;

  /*
  * N.B.
  * */
  @JsonIgnore
  public String getPropsName() {
    return this.props.getName();
  }

  @JsonIgnore
  public String getMain() {
    return this.props.getMain();
  }

  @JsonIgnore
  public String getDescription() {
    return this.props.getDescription();
  }

  @JsonIgnore
  public Properties getProperties() {
    return this.props.getProperties();
  }

  @JsonIgnore
  public Object getProp(String key) {
    return this.props.getProperties().get(key);
  }
}
