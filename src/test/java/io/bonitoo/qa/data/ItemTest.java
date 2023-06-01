package io.bonitoo.qa.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.data.*;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.plugin.*;
import io.bonitoo.qa.plugin.eg.CounterItemPlugin;
import io.bonitoo.qa.plugin.eg.PiItemGenPlugin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void createDoubleItemTest() throws JsonProcessingException {
        ItemConfig conf = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4);
        Item item = Item.of(conf);

        ObjectWriter ow = new ObjectMapper(new YAMLFactory()).writer();

        assertInstanceOf(Double.class, item.getVal());
        assertTrue(item.asDouble() < ((ItemNumConfig)conf).getMax());
        assertTrue(item.asDouble() > ((ItemNumConfig)conf).getMin());
    }

    @Test
    public void createLongItemTest(){
        ItemConfig conf = new ItemNumConfig("testLong", "someVal", ItemType.Long, 0, 10, 4);
        Item item = Item.of(conf);

        assertInstanceOf(Long.class, item.getVal());
        assertTrue(item.asLong() <= ((ItemNumConfig)conf).getMax());
        assertTrue(item.asLong() >= ((ItemNumConfig)conf).getMin());
    }

    @Test
    public void createStringItemTest(){
        ItemConfig conf = new ItemStringConfig("testString", "test", ItemType.String, Arrays.asList("TEST STRING"));
        Item item = Item.of(conf);

        assertInstanceOf(String.class, item.getVal());
        assertEquals("TEST STRING", item.asString());
    }

    @Test
    public void builtInItemTest(){
        ItemConfig conf = new ItemConfig("testBuiltIn", "temp", ItemType.BuiltInTemp, NumGenerator.class.getName());
        Item item = Item.of(conf);

        assertInstanceOf(Double.class, item.getVal());
        assertTrue(item.asDouble() > 0 && item.asDouble() < 40);
    }

    @Test
    public void getInextistantItem(){

        assertThrowsExactly(RuntimeException.class,
                () -> ItemConfigRegistry.get("zyxwvutsrqponmlkjihgfedcba"),
                "Item Configuration named zyxwvutsrqponmlkjihgfedcba not found");
    }


/*    public static class PiItemGenPlugin extends ItemGenPlugin {
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

 */

    @Test
    public void pluginItemTest() throws ClassNotFoundException,
      PluginConfigException {

        Properties generalProps = new Properties();
        generalProps.put("plugin.main", PiItemGenPlugin.class.getName());
        generalProps.put("plugin.name", "PiItemGenerator");
        generalProps.put("plugin.description", "GeneratesPI");
        generalProps.put("plugin.version","0.0.1");
        generalProps.put("plugin.type", "Item");
        generalProps.put("plugin.resultType", "Double");
        generalProps.put("plugin.label", "pi");

        PluginProperties props = new PluginProperties(generalProps);

        PiItemGenPlugin plugin = new PiItemGenPlugin(props, null, true);

        plugin.setDataConfig(new ItemPluginConfig(props,props.getName() + "01"));

        assertEquals(PluginResultType.Double, plugin.getResultType());
        assertEquals(props.getName() + "01", plugin.getDataConfig().getName());

        Item item = Item.of(plugin.getItemConfig());

        assertEquals(Math.PI, item.asDouble());

    }
    
    @Test
    public void pluginItemTestSeparateInstances(){
        PluginProperties props = new PluginProperties(CounterItemPlugin.class.getName(),
          "CounterPlugin",
          "count",
          "A simple test plugin",
          "0.1",
          PluginType.Item,
          PluginResultType.Long,
          new Properties());

        ItemConfig conf = new ItemPluginConfig(props, "testPluginConf");

        CounterItemPlugin plugin1 = new CounterItemPlugin(props, conf, true);
        CounterItemPlugin plugin2 = new CounterItemPlugin(props, conf, true);

        Item it1 = new Item(conf, 0, plugin1);
        Item it2 = new Item(conf, 0, plugin2);

        for(int i = 0; i < 5; i++) {
            it1.update();
        }

        it2.update();
        it2.update();

        assertEquals(5L, it1.asLong());
        assertEquals(2L, it2.asLong());
    }

    @Test
    public void numItemTestSeparateInstances(){
        ItemConfig conf = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4);

        Item item1 = Item.of(conf);
        Item item2 = Item.of(conf);

        assertNotEquals(item1.getGenerator(), item2.getGenerator());
    }

    @Test
    public void itemConfigCopyTest(){
        PluginProperties props = new PluginProperties(CounterItemPlugin.class.getName(),
          "CounterPlugin",
          "count",
          "A simple test plugin",
          "0.1",
          PluginType.Item,
          PluginResultType.Long,
          new Properties());


        ItemConfig configNum = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4);
        ItemConfig configString = new ItemStringConfig("testString", "lalala", ItemType.String,
          Arrays.asList("Do", "Re", "Mi", "Fa", "Sol", "La", "Ti"));
        ItemConfig configPlugin = new ItemPluginConfig(props, "testPluginConf");

        ItemConfig newConfigNum = new ItemNumConfig((ItemNumConfig) configNum);
        ItemConfig newConfigString = new ItemStringConfig((ItemStringConfig) configString);
        ItemConfig newConfigPlugin = new ItemPluginConfig((ItemPluginConfig) configPlugin);

        ((ItemNumConfig)newConfigNum).setMax(1000);
        ((ItemNumConfig)newConfigNum).setMin(-1000);
        ((ItemNumConfig)newConfigNum).setPeriod(1);
        newConfigNum.setLabel("asdf");

        assertNotEquals(configNum.hashCode(), newConfigNum.hashCode());
        assertNotEquals(configNum.getLabel(), newConfigNum.getLabel());
        assertNotEquals(((ItemNumConfig)configNum).getMax(), ((ItemNumConfig)newConfigNum).getMax());
        assertNotEquals(((ItemNumConfig)configNum).getMin(), ((ItemNumConfig)newConfigNum).getMin());
        assertNotEquals(((ItemNumConfig)configNum).getPeriod(), ((ItemNumConfig)newConfigNum).getPeriod());

        ((ItemStringConfig)newConfigString).getValues().add("BAAA!");
        ((ItemStringConfig)newConfigString).getValues().set(0, "Cee");
        newConfigString.setLabel("note");

        assertNotEquals(configString.hashCode(), newConfigString.hashCode());
        assertNotEquals(configString.getLabel(), newConfigString.getLabel());
        assertNotEquals(((ItemStringConfig)configString).getValues(), ((ItemStringConfig)newConfigString).getValues());

        newConfigPlugin.setName("localPluginCNF");
        newConfigPlugin.setLabel("index");
        assertNotEquals(configPlugin.hashCode(), newConfigPlugin.hashCode());
        assertNotEquals(configPlugin.getLabel(), newConfigPlugin.getLabel());

    }

    @Test
    public void uniquenessOfItemConfigInItemTest(){
        PluginProperties props = new PluginProperties(CounterItemPlugin.class.getName(),
          "CounterPlugin",
          "count",
          "A simple test plugin",
          "0.1",
          PluginType.Item,
          PluginResultType.Long,
          new Properties());


        ItemConfig configNum = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4);
        ItemConfig configString = new ItemStringConfig("testString", "lalala", ItemType.String,
          Arrays.asList("Do", "Re", "Mi", "Fa", "Sol", "La", "Ti"));
        ItemConfig configPlugin = new ItemPluginConfig(props, "testPluginConf");

        Item itNum = Item.of(configNum);
        Item itString = Item.of(configString);
        Item itPlugin = Item.of(configPlugin);

        // master copy in registry is unchanged
        assertEquals(configNum.hashCode(), ItemConfigRegistry.get(configNum.getName()).hashCode());
        // item received a copy of master
        assertNotEquals(ItemConfigRegistry.get(configNum.getName()).hashCode(), itNum.getConfig().hashCode());

        // master copy in registry is unchanged
        assertEquals(configString.hashCode(), ItemConfigRegistry.get(configString.getName()).hashCode());
        // item received a copy of master
        assertNotEquals(ItemConfigRegistry.get(configString.getName()).hashCode(), itString.getConfig().hashCode());

        // master copy in registry is unchanged
        assertEquals(configPlugin.hashCode(), ItemConfigRegistry.get(configPlugin.getName()).hashCode());
        // item received a copy of master
        assertNotEquals(ItemConfigRegistry.get(configPlugin.getName()).hashCode(), itPlugin.getConfig().hashCode());

    }





}
