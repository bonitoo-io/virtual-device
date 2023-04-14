package io.bonitoo.qa.data;

import io.bonitoo.qa.conf.data.*;
import io.bonitoo.qa.plugin.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void createDoubleItemTest(){
        ItemConfig conf = new ItemNumConfig("testDouble", ItemType.Double, 0, 10, 4);
        Item item = Item.of(conf);

        assertInstanceOf(Double.class, item.getVal());
        assertTrue(item.asDouble() < ((ItemNumConfig)conf).getMax());
        assertTrue(item.asDouble() > ((ItemNumConfig)conf).getMin());
    }

    @Test
    public void createLongItemTest(){
        ItemConfig conf = new ItemNumConfig("testLong", ItemType.Long, 0, 10, 4);
        Item item = Item.of(conf);

        assertInstanceOf(Long.class, item.getVal());
        assertTrue(item.asLong() <= ((ItemNumConfig)conf).getMax());
        assertTrue(item.asLong() >= ((ItemNumConfig)conf).getMin());
    }

    @Test
    public void createStringItemTest(){
        ItemConfig conf = new ItemStringConfig("testString", ItemType.String, Arrays.asList("TEST STRING"));
        Item item = Item.of(conf);

        assertInstanceOf(String.class, item.getVal());
        assertEquals("TEST STRING", item.asString());
    }

    @Test
    public void builtInItemTest(){
        ItemConfig conf = new ItemConfig("testBuiltIn", ItemType.BuiltInTemp);
        Item item = Item.of(conf);

        assertInstanceOf(Double.class, item.getVal());
        // see utils.Generator.genTemperature()
        assertTrue(item.asDouble() > 0 && item.asDouble() < 40);
    }

    @Test
    public void getInextistantItem(){

        assertThrowsExactly(RuntimeException.class,
                () -> ItemConfigRegistry.get("zyxwvutsrqponmlkjihgfedcba"),
                "Item Configuration named zyxwvutsrqponmlkjihgfedcba not found");
    }


    public static class PiItemGenPlugin extends ItemGenPlugin {
        public PiItemGenPlugin(String name, boolean enabled, ItemConfig config, PluginProperties props) {
            super(name, enabled, config, props);
        }

        public PiItemGenPlugin(){
            this.name = null;
            this.props = null;
            this.enabled = false;
            this.dataConfig = null;
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

        PluginProperties props = new PluginProperties(generalProps);

        PiItemGenPlugin plugin = new PiItemGenPlugin(props.getName(), true, null, props);

        plugin.setDataConfig(new ItemPluginConfig(props.getName(), props.getName() + "01", plugin));

        assertEquals(PluginResultType.Double, plugin.getResultType());
        assertEquals(props.getName() + "01", plugin.getDataConfig().getName());

        Item item = Item.of(plugin.getItemConfig());

        assertEquals(Math.PI, item.asDouble());

    }

}
