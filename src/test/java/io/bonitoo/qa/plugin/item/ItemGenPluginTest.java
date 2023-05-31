package io.bonitoo.qa.plugin.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.plugin.*;
import io.bonitoo.qa.plugin.eg.CounterItemPlugin;
import io.bonitoo.qa.plugin.eg.EmptyItemGenPlugin;
import io.bonitoo.qa.plugin.eg.PiItemGenPlugin;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import io.bonitoo.qa.plugin.util.JarTool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ItemGenPluginTest {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String DEFAULT_VALUE = "Foo";
  private static final File pluginDir = new File("src/test/plugins");
  private static final String testJarName = pluginDir.getPath() + "/itemPluginTest.jar";

  @BeforeAll
  public static void createJar() throws IOException {
    // Start by creating a simple plugin jar
    if(!pluginDir.exists()){
      if(!pluginDir.mkdirs()){
        throw new RuntimeException("Failed to make dir " + pluginDir);
      };
    }

    JarTool jTool = new JarTool();
    jTool.startManifest();
    jTool.addToManifest("Main-Class", CounterItemPlugin.class.getName());
    JarOutputStream target = jTool.openJar(testJarName);

    logger.debug(String.format("Path replacements %s.class", CounterItemPlugin.class.getName().replace(".", "/")));

    jTool.addFile(target, System.getProperty("user.dir")
        + "/target/test-classes",
      System.getProperty("user.dir")
        + "/target/test-classes/" + CounterItemPlugin.class.getName().replace(".", "/") + ".class");

    jTool.addRenamedFile(target, System.getProperty("user.dir")
        + "/src/test/resources",
      System.getProperty("user.dir")
        + "/src/test/resources/counterItemPlugin.props",
      "plugin.props"
    );

    target.close();
  }

  @AfterAll
  public static void cleanUp(){
    File pluginFile = new File(testJarName);
    pluginFile.deleteOnExit();
    pluginDir.deleteOnExit();
  }

  @AfterEach
  public void cleanUpDynamic(){
    // N.B. removes from registry, but loaded class remains until garbage collected
    ItemPluginMill.pluginPackMap.remove("CounterItemPlugin");
  }

  @Test
  public void createEmptyItemPluginTest(){
    PluginProperties props = new PluginProperties(EmptyItemGenPlugin.class.getName(),
      "Test Item Plugin",
      "val",
      "A Test Plugin",
      "0.0.1",
      PluginType.Item,
      PluginResultType.Double,
      new Properties()
      );
    ItemConfig itemConfig = new ItemConfig("TestItemConfig", "plug", ItemType.Plugin, props.getMain());
    EmptyItemGenPlugin plugin = new EmptyItemGenPlugin(props, itemConfig, false);
    assertEquals(EmptyItemGenPlugin.class.getName(), plugin.getMain());
    assertEquals(props.getName(), plugin.getPropsName());
    assertEquals(props.getDescription(), plugin.getDescription());
    assertEquals(props.getVersion(), plugin.getVersion());
    assertFalse(plugin.isEnabled());
    assertEquals(itemConfig, plugin.getDataConfig());
    plugin.onLoad();
    assertNull(plugin.genData());
    assertTrue(plugin.onEnable());
    assertTrue(plugin.isEnabled());
    //assertEquals(DEFAULT_VALUE + ":", plugin.genData());
    assertEquals(DEFAULT_VALUE, plugin.genData());
   // assertEquals("Foo:A3.14B7", plugin.genData('A', 3.14, 'B', '7'));
    assertEquals(PluginType.Item, plugin.getType());
    assertEquals(PluginResultType.Double, plugin.getResultType());
    assertFalse(plugin.onDisable());
    assertFalse(plugin.isEnabled());
  }

  // N.B. test relies on resource/counterItemPlugin.props
  @Test
  public void itemPluginLoadTest() throws IOException {

    File pluginFile = new File(testJarName);

    try {
      Class<? extends Plugin> clazz = PluginLoader.loadPlugin(pluginFile);
      assertTrue(ItemPluginMill.pluginPackMap.containsKey("CounterItemPlugin"));
      CounterItemPlugin counterPlugin = (CounterItemPlugin) ItemPluginMill.genNewInstance("CounterItemPlugin", null);
      assertEquals("CounterItemPlugin", counterPlugin.getPropsName());
      assertEquals(CounterItemPlugin.class.getName(), counterPlugin.getMain());
      assertEquals("0.1", counterPlugin.getVersion());
      assertEquals(1, counterPlugin.genData());
      assertEquals(2, counterPlugin.genData());
     // assertEquals(7, counterPlugin.genData(5));
      assertEquals(counterPlugin.getPluginName() + "Conf", counterPlugin.getDataConfig().getName());
      assertTrue(ItemPluginMill.pluginPackMap.containsKey(((ItemPluginConfig)counterPlugin.getDataConfig()).getPluginName()));
      assertTrue(ItemConfigRegistry.keys().contains(counterPlugin.getDataConfig().getName()));
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException |
             NoSuchMethodException | PluginConfigException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void itemPluginDeserializeTest()
    throws IOException, ClassNotFoundException, PluginConfigException,
    InvocationTargetException, NoSuchMethodException, InstantiationException,
    IllegalAccessException, NoSuchFieldException {

    File pluginFile = new File(testJarName);
    Class<? extends Plugin> clazz = PluginLoader.loadPlugin(pluginFile);
    assertTrue(ItemPluginMill.pluginPackMap.containsKey("CounterItemPlugin"));
    CounterItemPlugin counterPlugin = (CounterItemPlugin) ItemPluginMill.genNewInstance("CounterItemPlugin", null);
    assertEquals("CounterItemPlugin", counterPlugin.getPropsName());

    String localYamlConf = "---\n" +
      "name: \"plugin01\"\n" +
      "type: \"Plugin\"\n" +
      "label: \"ct\"\n" +
      "pluginName: \"" + counterPlugin.getPluginName() + "\"\n" +
      "resultType: \"" + counterPlugin.getResultType() + "\"";

    ObjectMapper om = new ObjectMapper(new YAMLFactory());

    ItemConfig confPlugin = om.readValue(localYamlConf, ItemPluginConfig.class);
    counterPlugin.setDataConfig(confPlugin);

    assertEquals(confPlugin, counterPlugin.getDataConfig());

    Item it = Item.of((ItemPluginConfig)counterPlugin.getDataConfig());

    assertEquals(1L, it.asLong());
    assertEquals(2L, it.update().asLong());
    assertEquals(3L, it.update().asLong());

  }

  @Test
  public void itemPluginWithPrecPropTest() throws JsonProcessingException {

    Properties additional = new Properties();
    additional.setProperty("plugin.decimal.prec", "3");
    PluginProperties props = new PluginProperties(PiItemGenPlugin.class.getName(),
      "PiTestItemPlugin",
      "val",
      "A Test Plugin",
      "0.0.1",
      PluginType.Item,
      PluginResultType.Double,
      additional
    );

    PiItemGenPlugin plugin = new PiItemGenPlugin(props, null, true);
    plugin.setDataConfig(new ItemPluginConfig(props,props.getName() + "01"));

    Item item = Item.of(plugin.getItemConfig());

    ObjectWriter ow = new ObjectMapper().writer();

    String val = ow.writeValueAsString(item);

    assertEquals(3.141, Double.parseDouble(val));
    assertEquals(Math.PI, item.asDouble());

  }

  // verify that prec from config file has precedence
  @Test
  public void itemPluginWithPrecConfTest() throws JsonProcessingException {

    Properties additional = new Properties();
    additional.setProperty("plugin.decimal.prec", "2");
    PluginProperties props = new PluginProperties(PiItemGenPlugin.class.getName(),
      "PiTestItemPlugin",
      "val",
      "A Test Plugin",
      "0.0.1",
      PluginType.Item,
      PluginResultType.Double,
      additional
    );

    PiItemGenPlugin plugin = new PiItemGenPlugin(props, null, true);
    ItemPluginConfig conf = new ItemPluginConfig(props, props.getName() + "01");
    conf.setPrec(4);
    plugin.setDataConfig(conf);

    Item item = Item.of(plugin.getItemConfig());

    ObjectWriter ow = new ObjectMapper().writer();

    String val = ow.writeValueAsString(item);

    assertEquals(3.1415, Double.parseDouble(val));
    assertEquals(Math.PI, item.asDouble());

  }


  @Test
  public void itemPluginLiteralWithPrecTest() throws IOException, PluginConfigException {

    String pluginPropsStr = "plugin.main=" + PiItemGenPlugin.class.getName() + "\n" +
      "plugin.name=PiItemPlugin\n" +
      "plugin.description=A Test plugin return Math.PI\n" +
      "plugin.version=0.1\n" +
      "plugin.type=Item\n" +
      "plugin.label=pi\n" +
      "plugin.resultType=Double\n" +
      "plugin.decimal.prec=3";

    Properties rawProps = new Properties();
    rawProps.load(new StringReader(pluginPropsStr));
    PluginProperties props = new PluginProperties(rawProps);

    PiItemGenPlugin plugin = new PiItemGenPlugin(props, null, true);
    plugin.setDataConfig(new ItemPluginConfig(props,props.getName() + "01"));

    Item item = Item.of((ItemPluginConfig)plugin.getDataConfig());

    ObjectWriter ow = new ObjectMapper().writer();

    String val = ow.writeValueAsString(item);

    assertEquals(3.141, Double.parseDouble(val));
    assertEquals(Math.PI, item.asDouble());

  }

}
