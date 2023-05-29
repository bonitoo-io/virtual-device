package io.bonitoo.qa.plugin.sample;

import io.bonitoo.qa.VirtualDeviceRuntimeException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.bonitoo.qa.plugin.Plugin;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.PluginProperties;
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

  static Map<String, PluginPack> pluginPackMap = new HashMap<>();

  /**
   * Returns the plugin class from the registry matching the key.
   *
   * @param key -
   * @return - the plugin class.
   */
  public static Class<? extends Plugin> getPluginClass(String key) {
    if (pluginPackMap.containsKey(key)) {
      return pluginPackMap.get(key).pluginClass;
    }
    throw new VirtualDeviceRuntimeException(String.format("No class found for key %s", key));
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

  /**
   * Returns plugin properties for the plugin matching the key.
   *
   * @param key -
   * @return - the properties.
   */
  public static PluginProperties getPluginProps(String key) {
    if (pluginPackMap.containsKey(key)) {
      return pluginPackMap.get(key).pluginProps;
    }
    throw new VirtualDeviceRuntimeException(String.format("No properties found for key %s", key));
  }

  /**
   * Generates a new instance of the Sample Plugin.
   *
   * <p>Looks for a constructor for the SamplePlugin class matching the
   * parameters <code>PluginProperties</code>, <code>SamplePluginConfig</code>,
   * <code>Object[]</code>.  This will then be called with the relative arguments.</p>
   *
   * @param pluginName - for locating the class in the registry.
   * @param spConfig - a config for the sample to be generated.
   * @param args - arguments to be passed to the constructor.
   * @return - an instance of the Sample plugin.
   * @throws PluginConfigException -
   * @throws NoSuchMethodException -
   * @throws InvocationTargetException -
   * @throws InstantiationException -
   * @throws IllegalAccessException -
   */
  public static SamplePlugin genNewInstance(String pluginName,
                                            SamplePluginConfig spConfig,
                                            Object... args)
      throws PluginConfigException, NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {

    if (!pluginPackMap.containsKey(pluginName)) {
      throw new PluginConfigException("Attempt to create instance of unknown plugin class "
        + pluginName);
    }

    SamplePluginMill.PluginPack pack = pluginPackMap.get(pluginName);

    SamplePlugin plugin = (SamplePlugin) pack.pluginClass.getDeclaredConstructor(
        PluginProperties.class,
        SamplePluginConfig.class,
        Object[].class).newInstance(
        pack.pluginProps,
        spConfig,
        args
    );

    plugin.onLoad();

    plugin.setProps(pack.pluginProps);

    plugin.applyProps(pack.pluginProps);

    logger.info(String.format("Generated new instance %s:%s of plugin %s:%s",
        plugin.getProps().getName(), plugin.hashCode(),
        pluginName, pack.pluginClass.getName()));

    return plugin;
  }

  /**
   * Generates a new instance of a SamplePlugin.
   *
   * <p>Leverages the factory method <code>SamplePlugin of(Method init,
   * SamplePluginConfig config, PluginProperties props)</code> of the <code>SamplePlugin</code>
   * class.  It expects a all extensions of the <code>SamplePlugin</code> class to implement
   * a static method <code>create(SamplePluginConfig)</code>.  This method works like a
   * callback, that offers more subtle configuration of the plugin instance.</p>
   * <p><emphasis>Note</emphasis> - this is the preferred method for instantiating a new
   * plugin instance.</p>
   *
   * @param pluginName - name of the plugin in the registry.
   * @param spConfig - a SamplePluginConfig instance;
   * @return - an instance of SamplePlugin or a class extending SamplePlugin.
   * @throws PluginConfigException --
   * @throws InvocationTargetException --
   * @throws NoSuchMethodException --
   * @throws InstantiationException --
   * @throws IllegalAccessException --
   */

  public static SamplePlugin genNewInstance(String pluginName, SamplePluginConfig spConfig)
      throws PluginConfigException, InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    if (!pluginPackMap.containsKey(pluginName)) {
      throw new PluginConfigException("Attempt to create instance of unknown plugin class "
        + pluginName);
    }

    @SuppressWarnings("unchecked")
    Class<SamplePlugin> clazz = (Class<SamplePlugin>) getPluginClass(pluginName);
    //    System.out.println("DEBUG conforms " + getCreateMethod(clazz));

    try {
      Method m = getCreateMethod(clazz);
      return SamplePlugin.of(m, spConfig, getPluginProps(pluginName));
    } catch (NoSuchMethodException e) {
      throw new PluginConfigException(String.format("Cannot instantiate pluginClass %s. "
        + " It Must have a static \"create\" method with parameter: %s ",
        clazz.getName(), SamplePluginConfig.class.getName()), e);
    }
  }

  /**
   * A convenience method for instantiating a new SamplePlugin, that gets the plugin name
   * directly from the config file.
   *
   * @param config - a SamplePluginConfig
   * @return - an instance of a new SamplePlugin or one of its extending classes.
   * @throws PluginConfigException --
   * @throws InvocationTargetException --
   * @throws NoSuchMethodException --
   * @throws InstantiationException --
   * @throws IllegalAccessException --
   */
  public static SamplePlugin genNewInstance(SamplePluginConfig config)
      throws PluginConfigException, InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    return genNewInstance(config.getPlugin(), config);
  }

  public static Map<String, SamplePluginMill.PluginPack> getMap() {
    return pluginPackMap;
  }

  public static int size() {
    return pluginPackMap.size();
  }

  public static Set<String> getKeys() {
    return pluginPackMap.keySet();
  }

  protected static void clear() {
    pluginPackMap.clear();
  }

  protected static Method getCreateMethod(Class<?> clazz) throws NoSuchMethodException {
    Method[] methods = clazz.getDeclaredMethods();
    for (Method m : methods) {
      if (m.getName().equals("create")) {
        Class<?>[] paramTypes = m.getParameterTypes();
        // todo check return type of m is correct
        if (paramTypes.length == 1
            && SamplePluginConfig.class.isAssignableFrom(paramTypes[0])
            && Modifier.isStatic(m.getModifiers())) {
          Class<?> returnType = m.getReturnType();
          if (SamplePlugin.class.isAssignableFrom(returnType)) {
            return m;
          }
        }
      }
    }
    throw new NoSuchMethodException(
      String.format("%s does not contain required static method create(SamplePluginConfig)",
        clazz.getName())
    );
  }


}
