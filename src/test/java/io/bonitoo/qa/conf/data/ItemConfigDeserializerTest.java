package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.plugin.*;
import io.bonitoo.qa.plugin.eg.EmptyItemGenPlugin;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class ItemConfigDeserializerTest {

    static PluginProperties props = new PluginProperties(EmptyItemGenPlugin.class.getName(),
      "TestItemPlugin",
      "val",
      "A Test Plugin",
      "0.0.1",
      PluginType.Item,
      PluginResultType.Double,
      new Properties()
    );

    static EmptyItemGenPlugin plugin = new EmptyItemGenPlugin(props, false);


    static ItemConfig confDouble = new ItemNumConfig("doubleConf", "dbl", ItemType.Double, -5, 10, 1.0, NumGenerator.DEFAULT_DEV);
    static ItemConfig confLong = new ItemNumConfig("longConf", "lng", ItemType.Long, 0, 100, 1.0, NumGenerator.DEFAULT_DEV);

    static ItemConfig confString = new ItemStringConfig("stringConf", "str", ItemType.String, Arrays.asList("Pepe", "Lance", "Bongo"));

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
        ItemPluginMill.addPluginClass(plugin.getPluginName(), plugin.getClass(), props);

        confPlugin = new ItemPluginConfig(props,props.getName() + "Test01");

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
        ItemPluginMill.removePluginClass(plugin.getPluginName());
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

        assertEquals(configD, ItemConfigRegistry.get(confDouble.getName()));
        assertEquals(configL, ItemConfigRegistry.get(confLong.getName()));
        assertEquals(configS, ItemConfigRegistry.get(confString.getName()));
        assertEquals(configP, ItemConfigRegistry.get(confPlugin.getName()));
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

        assertEquals(configD, ItemConfigRegistry.get(confDouble.getName()));
        assertEquals(configL, ItemConfigRegistry.get(confLong.getName()));
        assertEquals(configS, ItemConfigRegistry.get(confString.getName()));
        assertEquals(configP, ItemConfigRegistry.get(confPlugin.getName()));

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

    @Test
    public void pluginDeserializerTest() throws JsonProcessingException {
        ObjectMapper omj = new ObjectMapper();
        ObjectMapper omy = new ObjectMapper(new YAMLFactory());

        String localYamlConf = "---\n" +
          "name: \"TestItemPluginTest01\"\n" +
          "type: \"Plugin\"\n" +
          "label: \"test01\"\n" +
          "pluginName: \"TestItemPlugin\"\n" +
          "resultType: \"Double\"";

        String localJsonConf = "{\n" +
          "  \"name\" : \"TestItemPluginTest01\",\n" +
          "  \"label\": \"test01\",\n" +
          "  \"type\" : \"Plugin\",\n" +
          "  \"pluginName\" : \"TestItemPlugin\",\n" +
          "  \"resultType\" : \"Double\"\n" +
          "}";

        ItemPluginConfig configPj = (ItemPluginConfig) omj.readValue(localJsonConf, ItemConfig.class);
        ItemPluginConfig configPy = (ItemPluginConfig) omy.readValue(localYamlConf, ItemConfig.class);

        Class<? extends Plugin> IGPj = ItemPluginMill.getPluginClass(configPj.getPluginName());

        Class<? extends Plugin> IGPy = ItemPluginMill.getPluginClass(configPy.getPluginName());

        assertEquals(EmptyItemGenPlugin.class.getName(), IGPj.getName());
        assertEquals(EmptyItemGenPlugin.class.getName(), IGPy.getName());
        assertEquals(props.getName(), ItemPluginMill.getPluginProps(configPj.getPluginName()).getName());
        assertEquals(props.getName(), ItemPluginMill.getPluginProps(configPy.getPluginName()).getName());

    }

    @Test
    public void doubleDeserializedWithPrecision() throws JsonProcessingException {
        ItemNumConfig configWPrec = new ItemNumConfig("doubleConf", "dbl", ItemType.Double, -5, 10, 1.0, NumGenerator.DEFAULT_DEV, 6);
        ObjectWriter yw = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();
        String configAsYaml = yw.writeValueAsString(configWPrec);
        ObjectMapper omy = new ObjectMapper(new YAMLFactory());
        ItemConfig configParsed = omy.readValue(configAsYaml, ItemConfig.class);
        assertEquals(configWPrec.getPrec(), ((ItemNumConfig)configParsed).getPrec());
    }

    @Test
    public void doubleDeserializeWithDevAndPrec() throws JsonProcessingException {
        ItemNumConfig configWDev = new ItemNumConfig("doubleConf", "dbl", ItemType.Double, -25, 50, 1.0, 0.17, 2);
        ObjectWriter yw = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();
        String configAsYaml = yw.writeValueAsString(configWDev);
//        System.out.printf("DEBUG config\n%s\n", configAsYaml);
        ObjectMapper omy = new ObjectMapper(new YAMLFactory());
        ItemConfig configParsed = omy.readValue(configAsYaml, ItemConfig.class);
        assertEquals(configWDev, configParsed);
    }

    @Test
    public void itemYamlDeserializeNoDev() throws JsonProcessingException {
        String itemConfYaml = "---\n" +
          "name: \"flowRate\"\n" +
          "label: \"cmps\"\n" +
          "type: Double\n" +
          "max: 30\n" +
          "min: 5\n" +
          "period: 2";

        ObjectMapper omy = new ObjectMapper(new YAMLFactory());
        ItemConfig config = omy.readValue(itemConfYaml, ItemConfig.class);

//        System.out.println("DEBUG config\n" + config);
        assertEquals(NumGenerator.DEFAULT_DEV, ((ItemNumConfig) config).getDev());
    }

    @Test
    public void itemConfigWithCount() throws JsonProcessingException {

        String icConfigYaml = "---\n" +
          "name: \"Foo\"\n" +
          "label: \"bar\"\n" +
          "type: \"Double\"\n" +
          "count: 3\n" +
          "max: 100.0\n" +
          "min: 0.0\n" +
          "period: 1.0\n" +
          "dev: 0.17";

        ItemNumConfig inc = new ItemNumConfig("Foo", "bar", ItemType.Double, 0, 100, 1.0, 0.17);
        inc.setCount(3);

        assertEquals(3, inc.getCount());

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        ItemNumConfig parsedConf = (ItemNumConfig) om.readValue(icConfigYaml, ItemConfig.class);

        assertEquals(3, parsedConf.getCount());

    }

    @Test
    public void itemConfigWithSerialTypeArray() throws JsonProcessingException {

        String itemConfYaml = "---\n" +
          "name: \"flowRate\"\n" +
          "label: \"cmps\"\n" +
          "type: Double\n" +
          "max: 30\n" +
          "min: 5\n" +
          "period: 2\n" +
          "count: 3\n" +
          "arType: Array";

        ObjectMapper omy = new ObjectMapper(new YAMLFactory());
        ItemConfig config = omy.readValue(itemConfYaml, ItemConfig.class);

//        System.out.println("DEBUG config.getSerialType " + config.getArType());

        assertEquals(ItemArType.Array, config.getArType());

    }

    @Test
    public void itemConfigWithSerialTypeObject() throws JsonProcessingException {

        String itemConfYaml = "---\n" +
          "name: \"flowRate\"\n" +
          "label: \"cmps\"\n" +
          "type: Double\n" +
          "max: 30\n" +
          "min: 5\n" +
          "period: 2\n" +
          "count: 3\n" +
          "arType: Object";

        ObjectMapper omy = new ObjectMapper(new YAMLFactory());
        ItemConfig config = omy.readValue(itemConfYaml, ItemConfig.class);

     //   System.out.println("DEBUG config.getSerialType " + config.getArType());

        assertEquals(ItemArType.Object, config.getArType());

    }

    @Test
    public void itemConfigWithSerialTypeFlat() throws JsonProcessingException {

        String itemConfYaml = "---\n" +
          "name: \"flowRate\"\n" +
          "label: \"cmps\"\n" +
          "type: Double\n" +
          "max: 30\n" +
          "min: 5\n" +
          "period: 2\n" +
          "count: 3\n" +
          "arType: Flat";

        ObjectMapper omy = new ObjectMapper(new YAMLFactory());
        ItemConfig config = omy.readValue(itemConfYaml, ItemConfig.class);

  //      System.out.println("DEBUG config.getSerialType " + config.getArType());

        assertEquals(ItemArType.Flat, config.getArType());

    }

    @Test
    public void itemConfigWithSerialTypeDefault() throws JsonProcessingException {

        String itemConfYaml = "---\n" +
          "name: \"flowRate\"\n" +
          "label: \"cmps\"\n" +
          "type: Double\n" +
          "max: 30\n" +
          "min: 5\n" +
          "period: 2\n" +
          "count: 3\n";

        ObjectMapper omy = new ObjectMapper(new YAMLFactory());
        ItemConfig config = omy.readValue(itemConfYaml, ItemConfig.class);

 //       System.out.println("DEBUG config.getSerialType " + config.getArType());

        assertEquals(ItemArType.Undefined, config.getArType());

    }

    @Test
    public void itemConfigWithSerialTypeInvalid() throws JsonProcessingException {

        String itemConfYaml = "---\n" +
          "name: \"flowRate\"\n" +
          "label: \"cmps\"\n" +
          "type: Double\n" +
          "max: 30\n" +
          "min: 5\n" +
          "period: 2\n" +
          "count: 3\n" +
          "arType: Invalid";

        ObjectMapper omy = new ObjectMapper(new YAMLFactory());

        assertThrows(IllegalArgumentException.class, () -> {
           ItemConfig config = omy.readValue(itemConfYaml, ItemConfig.class);
        });
    }
}
