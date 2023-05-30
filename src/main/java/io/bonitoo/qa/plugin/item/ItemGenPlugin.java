package io.bonitoo.qa.plugin.item;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.plugin.PluginProperties;

/**
 * Point from which Item Generator Plugins will be created.
 */
public abstract class ItemGenPlugin extends DataGenPlugin<ItemConfig> {

  public ItemGenPlugin(PluginProperties props, ItemConfig config, boolean enabled) {
    super(props, config, enabled);
  }

  /**
   * Zero args constructor.
   *
   * <p>Sets <code>props</code> and <code>dataConfig</code> to null
   * and <code>enabled</code> to false.</p>
   */
  public ItemGenPlugin() {
    this.props = null;
    this.enabled = false;
    this.dataConfig = null;
  }

  public ItemConfig getItemConfig() {
    return (ItemConfig) this.dataConfig;
  }

  /**
   * Utility method so other plugins can get the value without running a new data calculation.
   *
   * @return - implementations should return the current value held by the Item plugin.
   */
  public abstract Object getCurrentVal();

}
