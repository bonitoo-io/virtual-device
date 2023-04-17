package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;

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
   * Sets the Item data config associated with this Item data generator plugin.
   *
   * <p>Ensures that back references are up-to-date.</p>
   *
   * @param itemPluginConfig - the ItemPluginConfig to be associated with the data generator.
   */
  public void setPluginConfig(ItemPluginConfig itemPluginConfig) {
    if (itemPluginConfig.getItemGen() != this) {
      itemPluginConfig.setItemGen(this);
    }
    this.setDataConfig(itemPluginConfig);
  }
}
