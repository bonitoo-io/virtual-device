package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.conf.data.ItemConfig;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Point from which Item Generator Plugins will be created.
 */
//@AllArgsConstructor
public abstract class ItemGenPlugin extends DataGenPlugin<ItemConfig> {

  public ItemGenPlugin(String name, boolean enabled, ItemConfig config, PluginProperties props) {
    super(name, enabled, config, props);
  }

  public ItemGenPlugin() {
    this.name = null;
    this.props = null;
    this.enabled = false;
    this.dataConfig = null;
  }

  public ItemConfig getItemConfig() {
    return (ItemConfig) this.dataConfig;
  }
}
