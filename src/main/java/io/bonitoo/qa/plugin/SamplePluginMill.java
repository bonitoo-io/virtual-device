package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.SampleConfig;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for generating and managing Sample Plugins.
 */
public class SamplePluginMill {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @AllArgsConstructor
  static class PluginPack {
    Class<? extends Plugin> pluginClass;
    PluginProperties pluginProps;
  }

  static Map<String, SamplePluginMill.PluginPack> pluginPackMap = new HashMap<>();

  public static Class<? extends Plugin> getPluginClass(String key) {
    return pluginPackMap.get(key).pluginClass;
  }

  public static void addPluginClass(String key,
                                    Class<? extends Plugin> pluginClass,
                                    PluginProperties props) {
    pluginPackMap.put(key, new SamplePluginMill.PluginPack(pluginClass, props));
    logger.info(String.format("Added plugin to mill %s:%s", props.getName(), props.getMain()));
  }

  /**
   * Adds a class to the Sample Plugin registry.
   *
   * @param key - key by which the class can be retrieved.
   * @param props - properties of the plugin.
   * @param ucl - url loader for loading the class.
   * @throws ClassNotFoundException -
   * @throws NoSuchFieldException -
   * @throws IllegalAccessException -
   */
  public static void addPluginClass(String key, PluginProperties props, URLClassLoader ucl)
      throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    if (props.getMain() == null) {
      throw new RuntimeException("Cannot add create plugin class without plugin.main property");
    }
    @SuppressWarnings("unchecked")
    Class<Plugin> pluginClass = (Class<Plugin>) Class.forName(props.getMain(), true, ucl);

    pluginPackMap.put(key, new SamplePluginMill.PluginPack((Class<Plugin>) pluginClass, props));
    logger.info(String.format("Added plugin to mill %s:%s", props.getName(), props.getMain()));
  }

  public static PluginProperties getPluginProps(String key) {
    return pluginPackMap.get(key).pluginProps;
  }

  /**
   * Generates a new instance of the Sample Plugin.
   *
   * @param pluginName - for locating the class in the registry.
   * @param sampleConfig - a config for the sample to be generated.
   * @param args - arguments to be passed to the constructor.
   * @return - an instance of the Sample plugin.
   * @throws PluginConfigException -
   * @throws NoSuchMethodException -
   * @throws InvocationTargetException -
   * @throws InstantiationException -
   * @throws IllegalAccessException -
   */
  public static SamplePlugin genNewInstance(String pluginName, SampleConfig sampleConfig, Object... args)
      throws PluginConfigException, NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {

    if (!pluginPackMap.containsKey(pluginName)) {
      throw new PluginConfigException("Attempt to create instance of unknown plugin class "
        + pluginName);
    }

    SamplePluginMill.PluginPack pack = pluginPackMap.get(pluginName);

    SamplePlugin plugin = (SamplePlugin) pack.pluginClass.getDeclaredConstructor(
        PluginProperties.class,
        SampleConfig.class,
        Object[].class).newInstance(
        pack.pluginProps,
        sampleConfig,
        args
    );

    plugin.setProps(pack.pluginProps);

    // instances get stored directly in config from where they get called
    // this pattern follows pattern used in Item class for the default generator
    // TODO resolve updateArgs for new null config
    //    if (sampleConfig == null) {
    //      plugin.setDataConfig(new SampleConfig(SamplePluginMill.getPluginProps(pluginName),
    //        pluginName + "Conf",
    //        null));
    //      plugin.setId(sampleConfig.getId());
    //      plugin.setTopic(sampleConfig.getTopic());
    /*if (pack.pluginProps.getPrec() != null) {
        ((ItemPluginConfig) plugin.getDataConfig()).setPrec(pack.pluginProps.getPrec());
      }
    } else {
      plugin.setDataConfig(pluginDataConfig);
    } */
    plugin.onLoad();

    logger.info(String.format("Generated new instance %s:%s of plugin %s:%s",
        plugin.getProps().getName(), plugin.hashCode(),
        pluginName, pack.pluginClass.getName()));

    return plugin;
  }
}
