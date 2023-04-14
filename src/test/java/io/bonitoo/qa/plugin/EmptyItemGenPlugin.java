package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.ItemConfig;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmptyItemGenPlugin extends ItemGenPlugin{

  private static final String DEFAULT_VALUE = "Foo";

    String value;

    public EmptyItemGenPlugin(String name, boolean enabled, ItemConfig config, PluginProperties props) {
      super(name, enabled, config, props);
    }

    @Override
    public void onLoad() {
      value = DEFAULT_VALUE;
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