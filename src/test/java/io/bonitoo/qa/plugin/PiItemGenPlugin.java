package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.ItemConfig;

public class PiItemGenPlugin extends ItemGenPlugin {
        public PiItemGenPlugin(PluginProperties props, ItemConfig config, boolean enabled) {
            super(props, config, enabled);
        }

        public PiItemGenPlugin(){
         //   this.name = null;
            this.props = null;
            this.enabled = false;
            this.dataConfig = null;
        }

        @Override
        public Double getCurrentVal() {
            return Math.PI;
        }

        @Override
        public void onLoad() {
            enabled = true;
        }

        @Override
        public Object genData(Object... args) {
            return Math.PI;
        }
}