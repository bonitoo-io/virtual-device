package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.plugin.ItemGenPlugin;
import io.bonitoo.qa.plugin.ItemPluginMill;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.PluginResultType;
import io.bonitoo.qa.plugin.PluginType;
import java.lang.reflect.InvocationTargetException;
import lombok.Getter;
import lombok.Setter;

/**
 * A configuration file for Items leveraging Item Genertar Plugins.
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
  ItemGenPlugin itemGen;

  @JsonIgnore
  PluginType pluginType;

  /**
   * Instantiates an ItemPluginConfig.
   *
   * <p>Note that the Item plugin main class needs to be loaded before
   * this constructor is called.  A plugin matching <code>pluginName</code> is searched for
   * in the ItemPluginMill</p>
   *
   * @param pluginName - name of the plugin.  Must match the <code>plugin.name</code>
   *                   property in the plugin.props file.
   * @param name - name of this ItemConfig.  So that the item can be reused in a virtual device config.
   * @param resultType - The type returned by the <code>genData()</code> method
   *                   of the main class of the plugin.
   * @throws PluginConfigException - thrown if plugin config is misconfigured
   * @throws InvocationTargetException -
   * @throws NoSuchMethodException -
   * @throws InstantiationException -
   * @throws IllegalAccessException -
   */
  public ItemPluginConfig(String pluginName, String name, PluginResultType resultType) throws PluginConfigException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    this.type = ItemType.Plugin;
    this.name = name;
    this.pluginName = pluginName;
    this.resultType = resultType;
    this.itemGen = ItemPluginMill.genNewInstance(this.pluginName, null);
    this.pluginType = PluginType.Item;
    ItemConfigRegistry.add(this.name, this);
    this.itemGen.setPluginConfig(this);
  }

  /**
   * Instantiates an ItemPluginConfig.
   *
   * <p>Assigns a generator reference to the config <i>without</i> searching in
   * ItemPluginMill.</p>
   *
   * @param pluginName - name of the plugin.  Must match the <code>plugin.name</code>
   *    *                   property in the plugin.props file.
   * @param name - name of this ItemConfig.  So that the item can be reused in a VirtualDevice config.
   * @param generator - the Item data generator to be assigned to the configuration.
   */
  public ItemPluginConfig(String pluginName, String name, ItemGenPlugin generator) {
    this.type = ItemType.Plugin;
    this.name = name;
    this.pluginName = pluginName;
    this.itemGen = generator;
    this.resultType = generator.getResultType();
    this.pluginType = PluginType.Item;
    ItemConfigRegistry.add(this.name, this);
    this.itemGen.setPluginConfig(this);
  }

  @Override
  public String toString() {
    return String.format("%s,pluginName:%s,itemGen:%s", super.toString(), pluginName, itemGen);
  }

  /**
   * Sets the itemGen Item data generator for this config.  Ensures
   * that back references are up-to-date.
   *
   * @param itemGen - the ItemGenPlugin data generator to be assigned.
   */
  public void setItemGen(ItemGenPlugin itemGen) {
    if (itemGen.getItemConfig() != this) {
      itemGen.setPluginConfig(this);
    }
    this.itemGen = itemGen;
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }

    final ItemPluginConfig conf = (ItemPluginConfig)obj;
    return pluginName.equals(conf.getPluginName())
      && resultType == conf.getResultType()
      && itemGen == conf.getItemGen();
  }

}
