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
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class ItemTest {

    @Test
    public void createDoubleItemTest() throws JsonProcessingException {
        ItemConfig conf = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4.0, NumGenerator.DEFAULT_DEV);

        Item item = Item.of(conf);

        ObjectWriter ow = new ObjectMapper(new YAMLFactory()).writer();

        assertInstanceOf(Double.class, item.getVal());
        double spread = ((ItemNumConfig)conf).getMax() - ((ItemNumConfig)conf).getMin();
        double spreadMax = ((ItemNumConfig)conf).getMax() + (spread * ((ItemNumConfig)conf).getDev());
        double spreadMin = ((ItemNumConfig)conf).getMin() - (spread * ((ItemNumConfig)conf).getDev());
        assertTrue(item.asDouble() < spreadMax);
        assertTrue(item.asDouble() > spreadMin);
    }

    @Test
    public void createLongItemTest(){
        ItemConfig conf = new ItemNumConfig("testLong", "someVal", ItemType.Long, 0, 10, 4.0, NumGenerator.DEFAULT_DEV);
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
    public void getInextistantItem(){

        assertThrowsExactly(RuntimeException.class,
                () -> ItemConfigRegistry.get("zyxwvutsrqponmlkjihgfedcba"),
                "Item Configuration named zyxwvutsrqponmlkjihgfedcba not found");
    }

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
        ItemPluginConfig conf = new ItemPluginConfig(props, props.getName()+"01");

       // PiItemGenPlugin plugin = new PiItemGenPlugin(props, null, true);

        // plugin.setDataConfig(new ItemPluginConfig(props,props.getName() + "01"));

        PiItemGenPlugin plugin = (PiItemGenPlugin) Item.of(conf, props).getGenerator();

        assertEquals(PluginResultType.Double, plugin.getResultType());
        assertEquals(props.getName() + "01", plugin.getDataConfig().getName());

        //Item item = Item.of(plugin.getItemConfig());
        plugin.onLoad();

        assertEquals(Math.PI, plugin.getItem().asDouble());

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

        //CounterItemPlugin plugin1 = new CounterItemPlugin(props, conf, true);
        CounterItemPlugin plugin1 = (CounterItemPlugin) Item.of(conf, props).getGenerator();
        //CounterItemPlugin plugin2 = new CounterItemPlugin(props, conf, true);
        CounterItemPlugin plugin2 = (CounterItemPlugin) Item.of(conf, props).getGenerator();

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
        ItemConfig conf = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4.0, NumGenerator.DEFAULT_DEV);

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


        ItemConfig configNum = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4.0, NumGenerator.DEFAULT_DEV);
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
    public void uniquenessOfItemConfigInItemTest() throws ClassNotFoundException {
        PluginProperties props = new PluginProperties(CounterItemPlugin.class.getName(),
          "CounterPlugin",
          "count",
          "A simple test plugin",
          "0.1",
          PluginType.Item,
          PluginResultType.Long,
          new Properties());


        ItemConfig configNum = new ItemNumConfig("testDouble", "someVal", ItemType.Double, 0, 10, 4.0, NumGenerator.DEFAULT_DEV);
        ItemConfig configString = new ItemStringConfig("testString", "lalala", ItemType.String,
          Arrays.asList("Do", "Re", "Mi", "Fa", "Sol", "La", "Ti"));
        ItemConfig configPlugin = new ItemPluginConfig(props, "testPluginConf");

        // Plugins are now sought in the factory
        ItemPluginMill.addPluginClass(props.getName(), props);

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

    @Test
    public void generateNegativeNumbers(){
        double min = -40;
        double max = -1;
        double dev = 0.1;
        double spread = max - min;
        double spreadMax = max + (spread * dev);
        double spreadMin = min - (spread * dev);

        ItemConfig configNum = new ItemNumConfig("testDouble", "someVal", ItemType.Double, min, max, 0.5, 0.1);

        Item itNum = Item.of(configNum);

        for(int i = 0; i < 100; i++){
            itNum.update();
            assertTrue(itNum.asDouble() >= spreadMin && itNum.asDouble() <= spreadMax);
        }


    }

}
