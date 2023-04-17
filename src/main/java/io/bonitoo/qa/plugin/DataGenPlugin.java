package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.DataConfig;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public abstract class DataGenPlugin<T extends DataConfig> {

  protected PluginProperties props;

  protected DataConfig dataConfig;
  protected boolean enabled = false;

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

  public abstract Object genData(Object... args);

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

}
