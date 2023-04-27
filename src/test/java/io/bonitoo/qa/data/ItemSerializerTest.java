package io.bonitoo.qa.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.data.generator.DataGenerator;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.data.generator.SimpleStringGenerator;
import io.bonitoo.qa.plugin.CounterItemPlugin;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.PluginResultType;
import io.bonitoo.qa.plugin.PluginType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemSerializerTest {

  @Test
  public void simpleDoubleItemSerializeTest() throws JsonProcessingException {
    ItemConfig conf = new ItemNumConfig("testConf", "test", ItemType.Double, -1.0, 1.0, 1);
    NumGenerator ng = (NumGenerator) DataGenerator.create(conf.getGenClassName());

    Item it = new Item(Double.parseDouble("2.71"), conf, conf.getLabel(), ng);

    ObjectWriter ow = new ObjectMapper().writer();

    assertEquals(2.71, Double.parseDouble(ow.writeValueAsString(it)));
  }

  @Test
  public void simpleLongItemSerializeTest() throws JsonProcessingException {
    ItemConfig conf = new ItemNumConfig("testConf", "test", ItemType.Long, -100, 100, 1);
    NumGenerator ng = (NumGenerator) DataGenerator.create(conf.getGenClassName());

    Item it = new Item(Long.parseLong("42"), conf, conf.getLabel(), ng);

    ObjectWriter ow = new ObjectMapper().writer();

    assertEquals(42, Long.parseLong(ow.writeValueAsString(it)));
  }


  @Test
  public void simpleStringItemSerializeTest() throws JsonProcessingException {
    ItemConfig conf = new ItemStringConfig("testConf", "text", ItemType.String, Arrays.asList("Apple", "Banana", "Orange"));
    SimpleStringGenerator sg = (SimpleStringGenerator) DataGenerator.create(conf.getGenClassName());
    Item it = new Item(conf, "Apple", sg);

    ObjectWriter ow = new ObjectMapper().writer();
    String result = ow.writeValueAsString(it);
    assertEquals("Apple", result.replaceAll("^\"|\"$", ""));
  }

  @Test
  public void simplePluginItemSerializeTest() throws JsonProcessingException {

    PluginProperties props = new PluginProperties(CounterItemPlugin.class.getName(),
      "CounterPlugin",
      "count",
      "A simple test plugin",
      "0.1",
      PluginType.Item,
      PluginResultType.Long,
      new Properties());

    ItemConfig conf = new ItemPluginConfig(props, "testPluginConf", new Vector<>());

    CounterItemPlugin plugin1 = new CounterItemPlugin(props, conf, true);

    // System.out.println("DEBUG plugin " + plugin1.genData());
    plugin1.genData();

    Item it1 = new Item(conf, 0, plugin1);

    for(int i = 0; i < 5; i++) {
      it1.update();
    }

    CounterItemPlugin plugin2 = new CounterItemPlugin(props, conf, true);

    Item it2 = new Item(conf, 0, plugin2);

    it2.update();
    assertEquals(6L, it1.getVal());
    assertEquals(1L, it2.getVal());

    ObjectWriter ow = new ObjectMapper().writer();

    String result1 = ow.writeValueAsString(it1);
    String result2 = ow.writeValueAsString(it2);

    assertEquals("6", result1);
    assertEquals("1", result2);
    assertEquals("count", it1.getLabel());
    assertEquals("count", it2.getLabel());
  }

  @Test
  public void serializeDoubleWithPrecisionTest() throws JsonProcessingException {
    // TODO
    int prec = 3;

    ItemNumConfig configWPrec = new ItemNumConfig("confWPrec", "dbl", ItemType.Double, -5, 10, 1, prec);
    ItemNumConfig configNoPrec = new ItemNumConfig("confWPrec", "dbl", ItemType.Double, -5, 10, 1);

    Item itPrec = Item.of(configWPrec);
    Item itNoPrec = Item.of(configNoPrec);

    ObjectWriter ow = new ObjectMapper().writer();

    String valPrec = ow.writeValueAsString(itPrec);
    String valNoPrec = ow.writeValueAsString(itNoPrec);


    assertTrue(valPrec.split("\\.")[1].length() <= prec);
    assertTrue(valNoPrec.split("\\.")[1].length() > prec);
  }

}
