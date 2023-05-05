package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class SamplePluginMill {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @AllArgsConstructor
  static class PluginPack {
    Class<? extends SamplePlugin> pluginClass;
    PluginProperties pluginProps;
  }

  static Map<String, SamplePluginMill.PluginPack> pluginPackMap = new HashMap<>();

  public static Class<? extends SamplePlugin> getPluginClass(String key) {
    return pluginPackMap.get(key).pluginClass;
  }

  public static void addPluginClass(String key,
                                    Class<? extends SamplePlugin> pluginClass,
                                    PluginProperties props) {
    pluginPackMap.put(key, new SamplePluginMill.PluginPack(pluginClass, props));
    logger.info(String.format("Added plugin to mill %s:%s", props.getName(), props.getMain()));
  }

  public static void addPluginClass(String key, PluginProperties props, URLClassLoader ucl)
    throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    if (props.getMain() == null) {
      throw new RuntimeException("Cannot add create plugin class without plugin.main property");
    }
    @SuppressWarnings("unchecked")
    Class<? extends SamplePlugin> pluginClass = (Class<? extends SamplePlugin>)
      Class.forName(props.getMain(), true, ucl);

    pluginPackMap.put(key, new SamplePluginMill.PluginPack(pluginClass, props));
    logger.info(String.format("Added plugin to mill %s:%s", props.getName(), props.getMain()));
  }

  public static PluginProperties getPluginProps(String key) {
    return pluginPackMap.get(key).pluginProps;
  }

/*  public static SamplePlugin genNewInstance(String pluginName, SampleConfig sampleConfig)
    throws PluginConfigException, NoSuchMethodException, InvocationTargetException,
    InstantiationException, IllegalAccessException {

    if (!pluginPackMap.containsKey(pluginName)) {
      throw new PluginConfigException("Attempt to create instance of unknown plugin class "
        + pluginName);
    }

    SamplePluginMill.PluginPack pack = pluginPackMap.get(pluginName);

    SamplePlugin plugin = pack.pluginClass.getDeclaredConstructor().newInstance();

    plugin.setProps(pack.pluginProps);

    // instances get stored directly in config from where they get called
    // this pattern follows pattern used in Item class for the default generator
    // TODO resolve updateArgs for new null config
    if (sampleConfig == null) {
//      plugin.setDataConfig(new SampleConfig(SamplePluginMill.getPluginProps(pluginName),
//        pluginName + "Conf",
//        null));
      plugin.setId(sampleConfig.getId());
      plugin.setTopic(sampleConfig.getTopic());
      if (pack.pluginProps.getPrec() != null) {
        ((ItemPluginConfig) plugin.getDataConfig()).setPrec(pack.pluginProps.getPrec());
      }
    } else {
      plugin.setDataConfig(pluginDataConfig);
    }
    plugin.onLoad();

    logger.info(String.format("Generated new instance %s:%s of plugin %s:%s",
      plugin.getDataConfig().getName(), plugin.hashCode(),
      pluginName, pack.pluginClass.getName()));

    return plugin;
  }

 */

}
