package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.VirDevRuntimeException;
import io.bonitoo.qa.conf.data.SampleConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class SamplePluginMillTest {

  private static String defaultKey = SamplePluginTest.conf.getPlugin();

  @BeforeEach
  public void zeroMillAtStart(){
    SamplePluginMill.clear();
  }

  @AfterEach
  public void cleanup(){
    SamplePluginMill.clear();
  }

  public static void loadFromFile()
    throws MalformedURLException, NoSuchFieldException,
    ClassNotFoundException, IllegalAccessException {
    String userDir = System.getProperty("user.dir");

    File file = Objects.requireNonNull(new File(userDir + "/target/test-classes/io/bonitoo/qa/plugin/")
      .listFiles((dir, name) -> name.equals("SamplePluginTest$FooSamplePlugin.class")))[0];

    URL fileURL = new URL("file://" + file.getPath());
    URL[] urls = {fileURL};
    URLClassLoader ucl = new URLClassLoader(urls);

    SamplePluginMill.addPluginClass(defaultKey, SamplePluginTest.defaultProps, ucl);
  }

  @Test
  public void addSamplePluginByClass(){

    assertEquals(0, SamplePluginMill.size());

    SamplePluginMill.addPluginClass(defaultKey,
      SamplePluginTest.FooSamplePlugin.class,
      SamplePluginTest.defaultProps);

    assertEquals(1, SamplePluginMill.size());

    @SuppressWarnings("unchecked")
    Class<SamplePluginTest.FooSamplePlugin> clazz = (Class<SamplePluginTest.FooSamplePlugin>)
      SamplePluginMill.getPluginClass(defaultKey);

    assertEquals("FooSamplePlugin", clazz.getSimpleName());

    PluginProperties props = SamplePluginMill.getPluginProps(defaultKey);

    assertEquals(SamplePluginTest.defaultProps, props);

  }

  @Test
  public void addSamplePluginByClassName()
    throws NoSuchFieldException, ClassNotFoundException,
    IllegalAccessException, MalformedURLException {

    assertEquals(0, SamplePluginMill.size());

    loadFromFile();

    assertEquals(1, SamplePluginMill.size());

    Class<SamplePluginTest.FooSamplePlugin> clazz = (Class<SamplePluginTest.FooSamplePlugin>) SamplePluginMill.getPluginClass(defaultKey);

    assertEquals("FooSamplePlugin", clazz.getSimpleName());
    assertEquals(SamplePluginTest.defaultProps, SamplePluginMill.getPluginProps(defaultKey));
  }

  @Test
  public void generateSamplePluginTestWithArgs()
    throws MalformedURLException, NoSuchFieldException,
    ClassNotFoundException, IllegalAccessException, PluginConfigException,
    InvocationTargetException, NoSuchMethodException, InstantiationException,
    JsonProcessingException {

    assertEquals(0, SamplePluginMill.size());

    loadFromFile();

    SamplePluginTest.FooSamplePlugin fsp = (SamplePluginTest.FooSamplePlugin)
      SamplePluginMill.genNewInstance(defaultKey,
      SamplePluginTest.conf, "forget me not!", 100);

    assertEquals("PropertyVal", fsp.value);
    assertEquals(100, fsp.count);
    assertTrue(fsp.enabled);

    fsp.update();

    assertEquals(101, fsp.count);
    assertTrue(
      fsp.toJson().matches("\\{\"id\":\"ASDF-1234\",\"timestamp\":[0-9]*,\"value\":\"PropertyVal\",\"count\":101\\}")
    );

  }

  @Test
  public void generateSamplePluginTest()
    throws MalformedURLException, NoSuchFieldException,
    ClassNotFoundException, IllegalAccessException, PluginConfigException,
    InvocationTargetException, NoSuchMethodException, InstantiationException {

    assertEquals(0, SamplePluginMill.size());

    loadFromFile();

    SamplePluginTest.FooSamplePlugin fsp = (SamplePluginTest.FooSamplePlugin)
      SamplePluginMill.genNewInstance(defaultKey,
        SamplePluginTest.conf);

    assertEquals("PropertyVal", fsp.value);
    assertEquals(0, fsp.count);
    assertTrue(fsp.enabled);

  }

  @Test
  public void generateSamplePluginTestConfigOnly()
    throws MalformedURLException, NoSuchFieldException,
    ClassNotFoundException, IllegalAccessException, PluginConfigException,
    InvocationTargetException, NoSuchMethodException, InstantiationException {

    assertEquals(0, SamplePluginMill.size());

    loadFromFile();

    SamplePluginTest.FooSamplePlugin fsp = (SamplePluginTest.FooSamplePlugin)
      SamplePluginMill.genNewInstance(SamplePluginTest.conf);

    assertEquals("PropertyVal", fsp.value);
    assertEquals(0, fsp.count);
    assertTrue(fsp.enabled);

  }

  public static class BarSamplePlugin extends SamplePlugin {
    /**
     * Constructs a Sample Plugin.
     *
     * @param props  - plugin properties.
     * @param config - configuration for the generated sample.
     */
    public BarSamplePlugin(PluginProperties props, SampleConfig config) {
      super(props, config);
    }

    @Override
    public String toJson() throws JsonProcessingException {
      return null;
    }

    @Override
    public void applyProps(PluginProperties props) {

    }
  }

  @Test
  public void generateSamplePluginWithoutCreateMethod() throws PluginConfigException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    PluginProperties barProps = new PluginProperties(
       BarSamplePlugin.class.getName(),
      "BarTestPlugin",
      "msg",
      "A test sample plugin",
      "0.1",
      PluginType.Sample,
      PluginResultType.Json,
      new Properties(){{ put("some.val", "Ba ba bar"); }}
    );

    SamplePluginConfig barConf = new SamplePluginConfig("QWERT-A098",
      "BarPluginConfig",
      "test/bar",
      new ArrayList<>(),
      barProps.getName());

    SamplePluginMill.addPluginClass(barConf.getPlugin(),
      BarSamplePlugin.class,
      barProps);

    assertThrowsExactly(PluginConfigException.class,
      () -> SamplePluginMill.genNewInstance(barConf),
      "Cannot instantiate pluginClass " +
        "io.bonitoo.qa.plugin.SamplePluginMillTest$BarSamplePlugin.  " +
        "It Must have a static \"create\" method with parameter: " +
        "io.bonitoo.qa.plugin.SamplePluginConfig");
  }

  @Test
  public void getPluginClassInexistant(){
    final String testKey = "ABCD1234";
    assertThrowsExactly(VirDevRuntimeException.class, () -> SamplePluginMill.getPluginClass(testKey),
      String.format(" No class found for key %s", testKey));
  }

  @Test
  public void getPluginPropsInexistant(){
    final String testKey = "ABCD1234";
    assertThrowsExactly(VirDevRuntimeException.class, () -> SamplePluginMill.getPluginProps(testKey),
      String.format(" No properties found for key %s", testKey));
  }
}
