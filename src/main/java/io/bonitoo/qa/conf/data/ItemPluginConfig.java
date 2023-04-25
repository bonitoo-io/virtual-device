package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.plugin.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

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
  PluginType pluginType;

  public ItemPluginConfig(PluginProperties props, String name, Vector<String> updateArgs) {
    this.type = ItemType.Plugin;
    this.name = name;
    this.label = props.getLabel();
    this.pluginName = props.getName();
    this.resultType = props.getResultType();
    this.genClassName = props.getMain();
    this.pluginType = PluginType.Item;
    this.updateArgs = updateArgs;
    ItemConfigRegistry.add(this.name, this);
  }

  public ItemPluginConfig(PluginProperties props, String name) {
    this.type = ItemType.Plugin;
    this.name = name;
    this.label = props.getLabel();
    this.pluginName = props.getName();
    this.resultType = props.getResultType();
    this.genClassName = props.getMain();
    this.pluginType = PluginType.Item;
    this.updateArgs = new Vector<>(); // create empty update args
    ItemConfigRegistry.add(this.name, this);
  }

  @Override
  public String toString() {
    return String.format("%s,pluginName:%s", super.toString(), pluginName);
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
