package io.bonitoo.qa.plugin.item;

import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.plugin.Plugin;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.PluginProperties;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for storing plugin classes and generating instances.
 */
public class ItemPluginMill {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @AllArgsConstructor
  static class PluginPack {
    Class<? extends Plugin> pluginClass;
    PluginProperties pluginProps;
  }

  static Map<String, PluginPack> pluginPackMap = new HashMap<>();

  public static Class<? extends Plugin> getPluginClass(String key) {
    return pluginPackMap.get(key).pluginClass;
  }

  /**
   * Return the class matching the sought class name.
   *
   * <p>Throws a runtime exception if the class is not registered.</p>
   *
   * @param classname - the name of the sought class.
   * @return - the class stored in the registry.
   */
  public static Class<? extends Plugin> getPluginClassByName(String classname) {
    for (String key : pluginPackMap.keySet()) {
      if (pluginPackMap.get(key).pluginClass.getName().equals(classname)) {
        return pluginPackMap.get(key).pluginClass;
      }
    }
    throw new RuntimeException(String.format("Class %s is not a loaded plugin", classname));
  }

  public static void addPluginClass(String key,
                                    Class<? extends Plugin> pluginClass,
                                    PluginProperties props) {
    pluginPackMap.put(key, new PluginPack(pluginClass, props));
    logger.info(String.format("Added plugin to mill %s:%s", props.getName(), props.getMain()));
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
    Class<Plugin> pluginClass =
        (Class<Plugin>) Class.forName(props.getMain());
    pluginPackMap.put(key, new PluginPack(pluginClass, props));
    logger.info(String.format("Added plugin to mill %s:%s", props.getName(), props.getMain()));
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
    Class<Plugin> pluginClass = (Class<Plugin>)
        Class.forName(props.getMain(), true, ucl);

    pluginPackMap.put(key, new PluginPack(pluginClass, props));
    logger.info(String.format("Added plugin to mill %s:%s", props.getName(), props.getMain()));

  }


  protected static void checkKey(String key) {
    if (! pluginPackMap.containsKey(key)) {
      throw new VirDevConfigException(
        String.format("Plugin key: %s unknown.  Is it in the %s/ directory?",
          key,
          Config.getProp("plugins.dir")
        )
      );

    }
  }

  public static PluginProperties getPluginProps(String key) {
    checkKey(key);
    return pluginPackMap.get(key).pluginProps;
  }

  /**
   * Generate a new instance of a plugin and add it to ItemPluginRegistry,
   * also a default ItemPluginConfig is generated and added to the ItemConfigRegistry.
   *
   * @param pluginName - name of the plugin from plugin.props
   * @param pluginDataConfig - dataConfig for this instance, if null one is generated
   *                         using the name <code>pluginName+Conf</code>
   * @return - the new instance
   * @throws PluginConfigException -
   * @throws NoSuchMethodException -
   * @throws InvocationTargetException -
   * @throws InstantiationException -
   * @throws IllegalAccessException -
   */

  public static ItemGenPlugin genNewInstance(String pluginName, ItemPluginConfig pluginDataConfig)
      throws PluginConfigException, NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {

    if (pluginDataConfig == null) {
      throw new PluginConfigException("pluginDataConfig must not be null");
    }

    if (!pluginPackMap.containsKey(pluginName)) {
      throw new PluginConfigException("Attempt to create instance of unknown plugin class "
        + pluginName);
    }

    PluginPack pack = pluginPackMap.get(pluginName);

    ItemGenPlugin plugin = (ItemGenPlugin) Item.of(pluginDataConfig, pack.pluginProps)
        .getGenerator();

    if (pack.pluginProps.getPrec() != null) {
      ((ItemPluginConfig) plugin.getDataConfig()).setPrec(pack.pluginProps.getPrec());
    }

    plugin.onLoad();

    logger.info(String.format("Generated new instance %s:%s of plugin %s:%s",
        pluginDataConfig.getName(), plugin.hashCode(),
        pluginName, pack.pluginClass.getName()));

    return plugin;
  }

  public static void removePluginClass(String key) {
    pluginPackMap.remove(key);
    logger.info(String.format("Removed plugin %s", key));
  }

  public static Set<String> getKeys() {
    return pluginPackMap.keySet();
  }

  /**
   * Verify that a class of the classname argument exists in the plugin registry.
   *
   * @param classname - name of the class to be sought.
   * @return - true if the entry is found otherwise false.
   */
  public static boolean hasPluginClass(String classname) {
    for (String key : pluginPackMap.keySet()) {
      if (pluginPackMap.get(key).pluginClass.getName().equals(classname)) {
        return true;
      }
    }
    return false;
  }



}
