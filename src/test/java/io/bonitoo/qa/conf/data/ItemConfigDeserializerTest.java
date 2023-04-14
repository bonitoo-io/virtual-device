package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.plugin.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ItemConfigDeserializerTest {

    static PluginProperties props = new PluginProperties(EmptyItemGenPlugin.class.getName(),
      "TestItemPlugin",
      "A Test Plugin",
      "0.0.1",
      PluginType.Item,
      PluginResultType.Double,
      new Properties()
    );

    static EmptyItemGenPlugin plugin = new EmptyItemGenPlugin(props.getName(),
       false, new ItemConfig(props.getName(), ItemType.Plugin), props);

    static ItemConfig confDouble = new ItemNumConfig("doubleConf", ItemType.Double, -5, 10, 1);
    static ItemConfig confLong = new ItemNumConfig("longConf", ItemType.Long, 0, 100, 1);

    static ItemConfig confString = new ItemStringConfig("stringConf", ItemType.String, Arrays.asList("Pepe", "Lance", "Bongo"));

    static ItemConfig confPlugin; // = new ItemPluginConfig(props.getName(), "plugin01", plugin);


    static String confDoubleJson;
    static String confLongJson;
    static String confStringJson;

    static String confPluginJson;

    static String confDoubleYaml;
    static String confLongYaml;
    static String confStringYaml;

    static String confPluginYaml;

    @BeforeAll
    public static void setStringConstants() throws JsonProcessingException, PluginConfigException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ItemConfigRegistry.clear();
        ItemPluginMill.addPluginClass(plugin.getName(), plugin.getClass(), props);
        confPlugin = new ItemPluginConfig(props.getName(), props.getName() + "Test01",
          ItemPluginMill.genNewInstance(props.getName(), props.getName() + "Test01"));

        System.out.println("DEBUG ItemPluginMill.keys " + ItemPluginMill.getKeys());

        ObjectWriter jsonWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectWriter yamlWriter = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

        confDoubleJson = jsonWriter.writeValueAsString(confDouble);
        confLongJson = jsonWriter.writeValueAsString(confLong);
        confStringJson = jsonWriter.writeValueAsString(confString);
        confPluginJson = jsonWriter.writeValueAsString(confPlugin);

        confDoubleYaml = yamlWriter.writeValueAsString(confDouble);
        confLongYaml = yamlWriter.writeValueAsString(confLong);
        confStringYaml = yamlWriter.writeValueAsString(confString);
        confPluginYaml = yamlWriter.writeValueAsString(confPlugin);
    }

    @AfterAll
    public static void cleanUp(){
        ItemPluginMill.removePluginClass(plugin.getName());
    }

    @Test
    public void jsonDeserializeTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();

        ItemNumConfig configD = (ItemNumConfig)om.readValue(confDoubleJson, ItemConfig.class);
        ItemNumConfig configL = (ItemNumConfig)om.readValue(confLongJson, ItemConfig.class);
        ItemStringConfig configS = (ItemStringConfig)om.readValue(confStringJson, ItemConfig.class);
        ItemPluginConfig configP = (ItemPluginConfig)om.readValue(confPluginJson, ItemConfig.class);

        assertEquals(configD, confDouble);
        assertEquals(configL, confLong);
        assertEquals(configS, confString);
        assertEquals(configP.getPluginName(), ((ItemPluginConfig)confPlugin).getPluginName());
        assertEquals(configP.getType(), confPlugin.getType());
        assertEquals(configP.getResultType(), ((ItemPluginConfig)confPlugin).getResultType());
        // every config should have its own instance of this
        assertNotEquals(configP.getItemGen(), ((ItemPluginConfig)confPlugin).getItemGen());

        assertEquals(configD, ItemConfigRegistry.get(confDouble.getName()));
        assertEquals(configL, ItemConfigRegistry.get(confLong.getName()));
        assertEquals(configS, ItemConfigRegistry.get(confString.getName()));
        assertEquals(configP, ItemConfigRegistry.get(confPlugin.getName()));

        System.out.println("DEBUG configPluginJson " + confPluginJson);
    }

    @Test
    public void yamlDeserializerTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        ItemNumConfig configD = (ItemNumConfig)om.readValue(confDoubleYaml,ItemConfig.class);
        ItemNumConfig configL = (ItemNumConfig)om.readValue(confLongYaml, ItemConfig.class);
        ItemStringConfig configS = (ItemStringConfig) om.readValue(confStringYaml, ItemConfig.class);
        ItemPluginConfig configP = (ItemPluginConfig) om.readValue(confPluginYaml, ItemConfig.class);

        assertEquals(configD, confDouble);
        assertEquals(configL, confLong);
        assertEquals(configS, confString);
        assertEquals(configP.getPluginName(), ((ItemPluginConfig)confPlugin).getPluginName());
        assertEquals(configP.getType(), confPlugin.getType());
        assertEquals(configP.getResultType(), ((ItemPluginConfig)confPlugin).getResultType());
        // every config should have its own instance of this
        assertNotEquals(configP.getItemGen(), ((ItemPluginConfig)confPlugin).getItemGen());

        assertEquals(configD, ItemConfigRegistry.get(confDouble.getName()));
        assertEquals(configL, ItemConfigRegistry.get(confLong.getName()));
        assertEquals(configS, ItemConfigRegistry.get(confString.getName()));
        assertEquals(configP, ItemConfigRegistry.get(confPlugin.getName()));

        System.out.println("DEBUG configPluginJson " + confPluginYaml);

    }

    @Test
    public void nullPropertyDeserializeTest() throws JsonProcessingException {
        String badYaml = "---\n" +
          "name: \"profNode01\"\n" +
          "type: \"Double\"\n" +
          "max: 150\n" +
          "min: 0\n";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        assertThrowsExactly(VirDevConfigException.class,
          () -> om.readValue(badYaml, ItemConfig.class),
          "property \"period\" for node {\"name\":\"profNode01\"," +
            "\"type\":\"Double\",\"max\":150,\"min\":0} is null.  Cannot parse any further");

    }


}
