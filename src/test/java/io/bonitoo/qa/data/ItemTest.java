package io.bonitoo.qa.data;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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

}
