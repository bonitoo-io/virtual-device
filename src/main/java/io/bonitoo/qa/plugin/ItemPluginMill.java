package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.ItemPluginConfig;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;

/**
 * A class for storing plugin classes and generating instances.
 */
public class ItemPluginMill {

  @AllArgsConstructor
  static class PluginPack {
    Class<? extends ItemGenPlugin> pluginClass;
    PluginProperties pluginProps;
  }

  static Map<String, PluginPack> pluginPackMap = new HashMap<>();

  public static Class<? extends ItemGenPlugin> getPluginClass(String key) {
    return pluginPackMap.get(key).pluginClass;
  }

  public static void addPluginClass(String key,
                                    Class<? extends ItemGenPlugin> pluginClass,
                                    PluginProperties props) {
    pluginPackMap.put(key, new PluginPack(pluginClass, props));
  }

  /**
   * Adds a plugin class to the mill.
   *
   * <p>The class will be generated from the property <code>plugin.main</code>, if
   * such a class has been loaded.  Otherwise, a ClassNotFoundException is thrown.
   *
   * @param key - name of the plugin for locating it later.
   * @param props - properties for configuring plugin, including main class.
   * @throws ClassNotFoundException - thrown if main class from the properties has not been loaded.
   */
  public static void addPluginClass(String key, PluginProperties props)
      throws ClassNotFoundException {
    if (props.getMain() == null) {
      throw new RuntimeException("Cannot add create plugin class without plugin.main property");
    }
    @SuppressWarnings("unchecked")
    Class<? extends ItemGenPlugin> pluginClass = (Class<? extends ItemGenPlugin>) Class.forName(props.getMain());
    pluginPackMap.put(key, new PluginPack(pluginClass, props));
  }

  /**
   * Adds a plugin class to the mill.
   *
   *  <p>The class will be generated from the property <code>plugin.main</code>, if
   *  such a class has been loaded.  Otherwise, a ClassNotFoundException is thrown.
   *
   * @param key - name of the plugin for locating later
   * @param props - properties for configuring plugin, including main class.
   * @param ucl - Loader for locating the class in a jar file
   * @throws ClassNotFoundException - thrown if main class cannot be found
   */
  public static void addPluginClass(String key, PluginProperties props, URLClassLoader ucl)
    throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    if (props.getMain() == null) {
      throw new RuntimeException("Cannot add create plugin class without plugin.main property");
    }
    @SuppressWarnings("unchecked")
    Class<? extends ItemGenPlugin> pluginClass = (Class<? extends ItemGenPlugin>)
        Class.forName(props.getMain(), true, ucl);

    pluginPackMap.put(key, new PluginPack(pluginClass, props));
  }

  public static PluginProperties getPluginProps(String key) {
    return pluginPackMap.get(key).pluginProps;
  }

  /**
   * Generate a new instance of a plugin and add it to ItemPluginRegistry,
   * also a default ItemPluginConfig is generated and added to the ItemConfigRegistry.
   *
   * @param pluginName - name of the plugin from plugin.props
   * @param instanceName - name for this instance so that it can be found
   * @return - the new instance
   * @throws PluginConfigException -
   * @throws NoSuchMethodException -
   * @throws InvocationTargetException -
   * @throws InstantiationException -
   * @throws IllegalAccessException -
   */
  public static ItemGenPlugin genNewInstance(String pluginName, String instanceName)
      throws PluginConfigException, NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {

    if (!pluginPackMap.containsKey(pluginName)) {
      throw new PluginConfigException("Attempt to create instance of unknown plugin class "
        + pluginName);
    }

    PluginPack pack = pluginPackMap.get(pluginName);

    ItemGenPlugin plugin = pack.pluginClass.getDeclaredConstructor().newInstance();

    plugin.setProps(pack.pluginProps);
    plugin.setName(instanceName);

    // instances get stored directly in config from where they get called
    // this pattern follows pattern used in Item class for the default generator
    plugin.setDataConfig(new ItemPluginConfig(pluginName, instanceName, plugin));
    plugin.onLoad();

    return plugin;
  }

  public static void removePluginClass(String key) {
    pluginPackMap.remove(key);
  }

  public static Set<String> getKeys() {
    return pluginPackMap.keySet();
  }

}
