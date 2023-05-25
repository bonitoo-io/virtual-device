package io.bonitoo.qa.plugin.eg;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.plugin.item.ItemGenPlugin;
import io.bonitoo.qa.plugin.PluginProperties;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmptyItemGenPlugin extends ItemGenPlugin {

  private static final String DEFAULT_VALUE = "Foo";

    String value;

    public EmptyItemGenPlugin(PluginProperties props, ItemConfig config, boolean enabled) {
      super(props, config, enabled);
    }

  @Override
  public String getCurrentVal() {
    return value;
  }

  @Override
    public void onLoad() {
      value = DEFAULT_VALUE;
    }

  @Override
  public void applyProps(PluginProperties props) {
     // Holder
  }

  @Override
    public String genData(Object... args) {
      StringBuilder sb = new StringBuilder();
      for(Object obj: args){
        sb.append(obj.toString());
      }
      if (enabled) {
        return value + ":" + sb.toString();
      } else {
        return null;
      }
    }
  }