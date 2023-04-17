package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.plugin.util.JarTool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ItemGenPluginTest {

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

    System.out.println("DEBUG path replacements " + CounterItemPlugin.class.getName().replace(".", "/") + ".class");

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

  /*
  static class EmptyItemGenPlugin extends ItemGenPlugin{

    String value;

    public EmptyItemGenPlugin(String name, PluginProperties props, boolean enabled, ItemConfig config) {
      super(name, props, enabled, config);
    }

    @Override
    public void onLoad() {
      value = DEFAULT_VALUE;
    }

    @Override
    public String genData(Object... args) {
      StringBuilder sb = new StringBuilder();
      for(Object obj: args){
        sb.append(obj.toString());
      }
      if (enabled) {
        return value + ":" + sb.toString();
      } else {
        return null;
      }
    }
  } */

  @Test
  public void createEmptyItemPluginTest(){
    PluginProperties props = new PluginProperties(EmptyItemGenPlugin.class.getName(),
      "Test Item Plugin",
      "A Test Plugin",
      "0.0.1",
      PluginType.Item,
      PluginResultType.Double,
      new Properties()
      );
    ItemConfig itemConfig = new ItemConfig("TestItemConfig", ItemType.Plugin);
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
    assertEquals(DEFAULT_VALUE + ":", plugin.genData());
    assertEquals("Foo:A3.14B7", plugin.genData('A', 3.14, 'B', '7'));
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
      @SuppressWarnings("unchecked")
      Class<ItemGenPlugin> clazz = (Class<ItemGenPlugin>) PluginLoader.loadPlugin(pluginFile);
      assertTrue(ItemPluginMill.pluginPackMap.containsKey("CounterItemPlugin"));
      CounterItemPlugin counterPlugin = (CounterItemPlugin) ItemPluginMill.genNewInstance("CounterItemPlugin", null);
      assertEquals("CounterItemPlugin", counterPlugin.getPropsName());
      assertEquals(CounterItemPlugin.class.getName(), counterPlugin.getMain());
      assertEquals("0.1", counterPlugin.getVersion());
      assertEquals(1, counterPlugin.genData());
      assertEquals(2, counterPlugin.genData());
      assertEquals(7, counterPlugin.genData(5));
   //   System.out.println("DEBUG counterPlugin " + counterPlugin.getName());
      System.out.println("DEBUG counterPlugin.genData " + counterPlugin.genData());
      System.out.println("DEBUG counterPlugin.genData " + counterPlugin.genData());
      System.out.println("DEBUG counterPlugin.genData " + counterPlugin.genData(5));
      System.out.println("DEBUG DataConfig " + counterPlugin.getDataConfig());
      System.out.println("DEBUG ItemPluginMill pluginPackMap.keySet() " + ItemPluginMill.pluginPackMap.keySet());
      System.out.println("DEBUG ItemConfigRegistry keySet() " + ItemConfigRegistry.keys());
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException |
             NoSuchMethodException | PluginConfigException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void itemPluginZDeserializeTest()
    throws IOException, ClassNotFoundException, PluginConfigException,
    InvocationTargetException, NoSuchMethodException, InstantiationException,
    IllegalAccessException, NoSuchFieldException {

    System.out.println("DEBUG ItemPluginMill.keys " + ItemPluginMill.pluginPackMap.keySet());

    File pluginFile = new File(testJarName);
    @SuppressWarnings("unchecked")
    Class<ItemGenPlugin> clazz = (Class<ItemGenPlugin>) PluginLoader.loadPlugin(pluginFile);
    assertTrue(ItemPluginMill.pluginPackMap.containsKey("CounterItemPlugin"));
    CounterItemPlugin counterPlugin = (CounterItemPlugin) ItemPluginMill.genNewInstance("CounterItemPlugin", null);
    assertEquals("CounterItemPlugin", counterPlugin.getPropsName());

    String localYamlConf = "---\n" +
      "name: \"plugin01\"\n" +
      "type: \"Plugin\"\n" +
      "pluginName: \"" + counterPlugin.getPluginName() + "\"\n" +
      "resultType: \"" + counterPlugin.getResultType() + "\"";

    ObjectMapper om = new ObjectMapper(new YAMLFactory());

    // ItemConfig confPlugin = new ItemPluginConfig(counterPlugin.getPluginName(), "plugin01", counterPlugin.getResultType());
    ItemConfig confPlugin = om.readValue(localYamlConf, ItemPluginConfig.class);
    counterPlugin.setDataConfig(confPlugin);

    System.out.println("DEBUG counterPlugin.dataConf " + counterPlugin.getDataConfig());

    System.out.println("DEBUG Item.of " + Item.of((ItemPluginConfig)counterPlugin.getDataConfig()).asLong());
    System.out.println("DEBUG Item.of " + Item.of((ItemPluginConfig)counterPlugin.getDataConfig()).asLong());
    System.out.println("DEBUG Item.of " + Item.of((ItemPluginConfig)counterPlugin.getDataConfig()).asLong());

    // TODO finish this

  }

}
