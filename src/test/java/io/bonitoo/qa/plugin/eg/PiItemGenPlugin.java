package io.bonitoo.qa.plugin.eg;

import io.bonitoo.qa.plugin.item.ItemGenPlugin;
import io.bonitoo.qa.plugin.PluginProperties;

public class PiItemGenPlugin extends ItemGenPlugin {


        //public PiItemGenPlugin(PluginProperties props, ItemConfig config, boolean enabled) {
        //    super(props, config, enabled);
        //}

        public PiItemGenPlugin(PluginProperties props, boolean enabled){
          super(props, enabled);
        }

        public PiItemGenPlugin(){
         //   this.name = null;
            this.props = null;
            this.enabled = false;
         //   this.dataConfig = null;
        }

        @Override
        public Double getCurrentVal() {
            return (Double) item.getVal();
        }

        @Override
        public void onLoad() {
            item.setVal(Math.PI);
            enabled = true;
        }

  @Override
  public void applyProps(PluginProperties props) {
     // holder
  }

  /*
  @Override
        public Object genData(Object... args) {
            return Math.PI;
        }
*/
  @Override
  public Object genData() {
    return Math.PI;
  }
}