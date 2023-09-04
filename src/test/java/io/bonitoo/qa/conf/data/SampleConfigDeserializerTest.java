package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.Sample;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.plugin.sample.SamplePluginConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@Tag("unit")
public class SampleConfigDeserializerTest {

    static ItemConfig confDouble = new ItemNumConfig("doubleConf", "foo",  ItemType.Double, -5, 10, 1.0, NumGenerator.DEFAULT_DEV);
    static ItemConfig confLong = new ItemNumConfig("longConf", "bar", ItemType.Long, 0, 100, 1.0, NumGenerator.DEFAULT_DEV);

    static ItemConfig confString = new ItemStringConfig("stringConf", "wumpus", ItemType.String, Arrays.asList("Pepe", "Lance", "Bongo"));

    static SampleConfig confSample = new SampleConfig("ffffffff", "sampleConf", "test/sample",
            Arrays.asList(confDouble, confLong, confString));

    static SampleConfig confSampleItemStrings;

    static String confSampleJSON;
    static String confSampleYAML;

    static String confSampleItemStringsJSON = "{\n" +
            "   \"id\": \"ffffffff\",\n" +
            "   \"name\": \"sampleConf\",\n" +
            "   \"topic\": \"test/sample\",\n" +
            "   \"items\": [\n" +
            "      \"doubleConf\",\n" +
            "      \"longConf\",\n" +
            "      \"stringConf\"\n" +
            "   ]\n" +
            "}";

    static String confSampleItemStringsPluginJSON = "{\n" +
      "   \"id\": \"ffffffff\",\n" +
      "   \"plugin\": \"Foo Plugin\",\n" +
      "   \"name\": \"sampleConf\",\n" +
      "   \"topic\": \"test/sample\",\n" +
      "   \"items\": [\n" +
      "      \"doubleConf\",\n" +
      "      \"longConf\",\n" +
      "      \"stringConf\"\n" +
      "   ]\n" +
      "}";

    static String confSampleItemStringsYAML = "---\n" +
            "id: ffffffff\n" +
            "name: sampleConf\n" +
            "topic: test/sample\n" +
            "items:\n" +
            "- doubleConf\n" +
            "- longConf\n" +
            "- stringConf";

    @BeforeAll
    public static void setupValues() throws JsonProcessingException {
        confSampleItemStrings = new SampleConfig("eeeeeeee", "sampleConfStrings", "test/sample",
                new String[]{confDouble.getName(),confLong.getName(),confString.getName()});
        SampleConfigRegistry.clear();

        ObjectWriter jsonWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectWriter yamlWriter = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

        confSampleJSON = jsonWriter.writeValueAsString(confSample);

        confSampleYAML = yamlWriter.writeValueAsString(confSample);

    }

    @Test
    public void jsonConfigTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();

        SampleConfig sampleConf = om.readValue(confSampleJSON, SampleConfig.class);

        assertEquals(confSample, sampleConf);
    }

    @Test
    public void jsonConfigItemStringsTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();

        SampleConfig sampleConf = om.readValue(confSampleItemStringsJSON, SampleConfig.class);

        assertEquals(confSample, sampleConf);

    }

    @Test
    public void jsonConfigItemStringsPluginTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        SampleConfig sampleConf = om.readValue(confSampleItemStringsPluginJSON, SampleConfig.class);

        assertEquals(sampleConf.getPlugin(), "Foo Plugin");

        SamplePluginConfig spConf = om.readValue(confSampleItemStringsPluginJSON, SamplePluginConfig.class);

        assertEquals(sampleConf.getPlugin(), "Foo Plugin");

    }

    @Test
    public void yamlConfigTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        SampleConfig sampleConf = om.readValue(confSampleYAML, SampleConfig.class);

        assertEquals(confSample, sampleConf);
    }

    @Test
    public void yamlConfigItemStringsTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        SampleConfig sampleConf = om.readValue(confSampleYAML, SampleConfig.class);

        assertEquals(confSample, sampleConf);
    }

    @Test
    public void nullItemsSerializeTest() throws JsonProcessingException {
        String badYaml = "---\n" +
          "id: \"cokoliv\"\n" +
          "name: \"mlok\"\n" +
          "topic: \"testing/test\"";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        assertThrowsExactly(VirDevConfigException.class,
          () -> om.readValue(badYaml, SampleConfig.class), "property \"items\" for node " +
            "{\"id\":\"cokoliv\",\"name\":\"mlok\",\"topic\":\"testing/test\"} is null.  " +
            "Cannot parse any further");
    }

    @Test
    public void nullIdSerializeTest() throws JsonProcessingException {
        String badYaml = "---\n" +
           "name: \"foo\"";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        assertThrowsExactly(VirDevConfigException.class,
          () -> om.readValue(badYaml, SampleConfig.class), "property \"id\" " +
            "for node {\"name\":\"foo\"} is null.  Cannot parse any further");
    }

    @Test
    public void itemConfigWithCountTest() throws JsonProcessingException {

     /*   String sampleItemConfigYaml01 = "---\n" +
          "itemConf:\n" +
          "  name: \"Foo\"\n" +
          "  label: \"bar\"\n" +
          "  type: \"Double\"\n" +
          "  genClassName: \"io.bonitoo.qa.data.generator.NumGenerator\"\n" +
          "  max: 100.0\n" +
          "  min: 0.0\n" +
          "  period: 1.0\n" +
          "  dev: 0.17\n" +
          "count: 1";
          */

        String icConfigYaml = "---\n" +
          "name: \"Foo\"\n" +
          "label: \"bar\"\n" +
          "type: \"Double\"\n" +
          "genClassName: \"io.bonitoo.qa.data.generator.NumGenerator\"\n" +
          "count: 3\n" +
          "max: 100.0\n" +
          "min: 0.0\n" +
          "period: 1.0\n" +
          "dev: 0.17";

        //SampleItemConfig siConfig = new SampleItemConfig(
        //  new ItemNumConfig("Foo", "bar", ItemType.Double, 0, 100, 1.0, 0.17)
        //);

        ItemNumConfig inc = new ItemNumConfig("Foo", "bar", ItemType.Double, 0, 100, 1.0, 0.17);
        inc.setCount(3);

       // System.out.println("DEBUG siConfig.getItemConf() " + siConfig.getItemConf());
       // System.out.println("DEBUG siConfig.getCount() " + siConfig.getCount());

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        String incString = om.writeValueAsString(inc);

        System.out.println("DEBUG incString " + incString);


        ItemNumConfig parsedConf = (ItemNumConfig) om.readValue(icConfigYaml, ItemConfig.class);

        System.out.println("DEBUG parsedConf.count " + parsedConf);

    }

    @Test
    public void withItemArray() throws JsonProcessingException {

        // TODO add ItemPluginType to config test

        ItemNumConfig inc = new ItemNumConfig("Foo", "bar", ItemType.Double, 0, 100, 1.0, 0.17);
        ItemNumConfig rabbit = new ItemNumConfig("Rabbit", "mrkev", ItemType.Double, 1.0, 5.0, 1.0, 0.21);

        String testSampleYaml = "---\n" +
          "id: ffffffff\n" +
          "name: sampleConf\n" +
          "topic: test/sample\n" +
          "items:\n" +
          "  - Foo\n" +
          "  - Foo\n" +
          "  - Foo\n" +
          "  - from: \"Rabbit\"\n" +
          "    count: 5\n" +
          "  - name: \"Krtek\"\n" +
          "    label: \"ktk\"\n" +
          "    type: \"Double\" \n" +
          "    count: 3\n" +
          "    max: 100.0\n" +
          "    min: 0.0\n" +
          "    period: 1.0\n" +
          "    dev: 0.17\n" +
          "  - name: \"Jezek\"\n" +
          "    label: \"jzk\"\n" +
          "    type: \"Double\" \n" +
          "    max: 10.0\n" +
          "    min: -10.0\n" +
          "    period: 0.5\n" +
          "    dev: 0.17";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        SampleConfig sc = om.readValue(testSampleYaml, SampleConfig.class);

        assertEquals(4, sc.getItems().size());

        List<ItemConfig> fooItems = sc.getItems().stream().filter(ic -> ic.getName().equals("Foo")).collect(Collectors.toList());
        System.out.println("DEBUG fooItems " + fooItems.size());
        assertEquals(1, fooItems.size());
        assertEquals(3, fooItems.get(0).getCount());

        List<ItemConfig> rabbitItems = sc.getItems().stream().filter(ic -> ic.getName().equals("Rabbit")).collect(Collectors.toList());
        System.out.println("DEBUG rabbitItems " + rabbitItems.size());
        assertEquals(1, rabbitItems.size());
        assertEquals(5, rabbitItems.get(0).getCount());

        List<ItemConfig> krtekItems = sc.getItems().stream().filter(ic -> ic.getName().equals("Krtek")).collect(Collectors.toList());
        System.out.println("DEBUG krtekItems " + krtekItems.size());
        assertEquals(1, krtekItems.size());
        assertEquals(3, krtekItems.get(0).getCount());

        List<ItemConfig> jezekItems = sc.getItems().stream().filter(ic -> ic.getName().equals("Jezek")).collect(Collectors.toList());
        System.out.println("DEBUG jezekItems " + jezekItems.size());
        assertEquals(1, jezekItems.size());
        assertEquals(1, jezekItems.get(0).getCount());

        for(ItemConfig ic : sc.getItems()){
            System.out.println("DEBUG ic " + ic.getName());
        }

        System.out.println("DEBUG sc:\n" + om.writeValueAsString(sc));

// These statements to SampleTest
//        GenericSample gs = GenericSample.ofExperim(sc);

//        System.out.println("DEBUG gs " + gs.toJson());

    }

    @Test
    public void withArTypeTest() throws JsonProcessingException {

        String testSampleYaml = "---\n" +
          "id: ffffffff\n" +
          "name: sampleConf\n" +
          "topic: test/sample\n" +
          "arType: \"Flat\"\n" +
          "items:\n" +
          "  - name: \"Krtek\"\n" +
          "    label: \"ktk\"\n" +
          "    type: \"Double\"\n" +
          "    count: 3\n" +
          "    max: 100.0\n" +
          "    min: 0.0\n" +
          "    period: 1.0\n" +
          "    dev: 0.17";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        SampleConfig sc = om.readValue(testSampleYaml, SampleConfig.class);

        System.out.println("DEBUG sc.arType " + sc.getArType());

    }

}
