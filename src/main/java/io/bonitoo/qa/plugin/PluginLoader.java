package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.zip.ZipException;

/**
 * Loads a DataGenPlugin into the VM
 */
public class PluginLoader {

  private static Object loadPluginYamlConfig(File file) throws MalformedURLException {
    URL url = file.toURI().toURL();
    String jarUrl = "jar:" + url + "!/plugin.yml";

    InputStream input;
    URL inputUrl = new URL(jarUrl);

    try {
      JarURLConnection conn = (JarURLConnection) inputUrl.openConnection();
      input = conn.getInputStream();
      ObjectMapper om = new ObjectMapper(new YAMLFactory());
      return om.readValue(input, Object.class);
    } catch (IOException e) {
      System.out.println("WARNING: Caught IOException " + e);
      System.out.println("Abort reading config yml");
      return null;
    }
  }

  private static PluginProperties loadPluginProperties(File file) throws ZipException, IOException, PluginConfigException {
    URL url = file.toURI().toURL();
    String jarUrl = "jar:" + url + "!/plugin.props";

    InputStream input;
    URL inputUrl = new URL(jarUrl);
    JarURLConnection conn = (JarURLConnection) inputUrl.openConnection();
    input = conn.getInputStream();

    Properties properties = new Properties();

    properties.load(input);

    return new PluginProperties(properties);
  }

  /**
   * Loads a plugin from a plugin jar file
   *
   * @param file - the jar file to be loaded
   * @return - the main class of the plugin defined in the jar plugin file
   * @throws IOException - when there are problems loading the file
   * @throws ClassNotFoundException - when plugin class is not available
   * @throws PluginConfigException - when the plugin.props file of the plugin is illegal or incomplete
   */
  public static Class<? extends DataGenPlugin<?>> loadPlugin(File file) throws IOException,
    PluginConfigException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    if (!file.exists()) {
      return null;
    }

    PluginProperties props = loadPluginProperties(file);
    Object obj = loadPluginYamlConfig(file);
    if (obj != null) {
      System.out.println("DEBUG Obj " + obj);
    }

    URL url = file.toURI().toURL();
    String jarUrl = "jar:" + url + "!/";
    URL[] urls = {new URL(jarUrl)};
    URLClassLoader ucl = new URLClassLoader(urls);
    // add plugin class to registry and instantiate later as needed
    if (props.getType() == PluginType.Item) {
      ItemPluginMill.addPluginClass(props.getName(), props, ucl);
      return ItemPluginMill.getPluginClass(props.getName());
    } else if (props.getType() == PluginType.Sample) {
      throw new RuntimeException("Sample Plugin handling not yet implemented - TODO");
    } else {
      throw new PluginConfigException("Plugin must have a type of either Item or Sample");
    }
  }
}
