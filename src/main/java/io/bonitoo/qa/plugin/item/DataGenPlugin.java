package io.bonitoo.qa.plugin.item;

import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.data.generator.DataGenerator;
import io.bonitoo.qa.plugin.Plugin;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.PluginResultType;
import io.bonitoo.qa.plugin.PluginType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Base class for data configuration plugins.
 *
 * <p>N.B. ItemPluginConfig contains a reference to the plugin,
 * that gets called in the <code>Item.of</code>.</p>
 *
 * <p>TODO. A SamplePluginConfig will be similarly implemented</p>
 *
 * @param <T> - ItemConfig or SampleConfig
 */
// @AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public abstract class DataGenPlugin<T extends DataConfig> extends DataGenerator implements Plugin {

  protected PluginProperties props;

  protected boolean enabled = false;

  /**
   * Basic constructor for DataGenPlugin,  Called mainly by subclasses.
   *
   * @param props - properties for the plugin.
   * @param enabled - whether to start enabled or not.
   */
  public DataGenPlugin(PluginProperties props, boolean enabled) {
    this.props = props;
    this.enabled = enabled;
  }

  /**
   * Basic constructor for DataGenPlugin,  Called mainly by subclasses.
   *
   * @param props - properties for the plugin.
   */
  public DataGenPlugin(PluginProperties props) {
    this.props = props;
    this.enabled = false;
  }

  public String getPluginName() {
    return props.getName();
  }

  public boolean onEnable() {
    return enabled = true;
  }

  public boolean onDisable() {
    return enabled = false;
  }

  public abstract void onLoad();

  //public abstract Object genData(Object... args);

  public String getPropsName() {
    return props.getName();
  }

  public String getMain() {
    return props.getMain();
  }

  public String getDescription() {
    return props.getDescription();
  }

  public String getVersion() {
    return props.getVersion();
  }

  public PluginType getType() {
    return props.getType();
  }

  public PluginResultType getResultType() {
    return props.getResultType();
  }

  public ItemConfig getDataConfig() {
    return getItem().getConfig();
  }

}
