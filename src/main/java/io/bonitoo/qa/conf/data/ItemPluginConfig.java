package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.plugin.ItemGenPlugin;
import io.bonitoo.qa.plugin.ItemPluginMill;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.PluginResultType;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;

@Getter
@Setter
public class ItemPluginConfig extends ItemConfig {

  String pluginName;

  PluginResultType resultType;

  @JsonIgnore
  ItemGenPlugin itemGen;

  public ItemPluginConfig(String pluginName, String name, PluginResultType resultType) throws PluginConfigException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    this.type = ItemType.Plugin;
    this.name = name;
    this.pluginName = pluginName;
    this.resultType = resultType;
    this.itemGen = ItemPluginMill.genNewInstance(this.pluginName, this.name);
    ItemConfigRegistry.add(this.name, this);
  }

  public ItemPluginConfig(String pluginName, String name, ItemGenPlugin generator) {
    this.type = ItemType.Plugin;
    this.name = name;
    this.pluginName = pluginName;
    this.itemGen = generator;
    this.resultType = generator.getResultType();
    ItemConfigRegistry.add(this.name, this);
  }

  @Override
  public String toString() {
    return String.format("%s,pluginName:%s,itemGen:%s", super.toString(), pluginName, itemGen.getName());
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
