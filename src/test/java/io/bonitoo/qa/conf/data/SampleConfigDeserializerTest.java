package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.data.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class SampleConfigDeserializerTest {

    static ItemConfig confDouble = new ItemNumConfig("doubleConf", "foo",  ItemType.Double, -5, 10, 1);
    static ItemConfig confLong = new ItemNumConfig("longConf", "bar", ItemType.Long, 0, 100, 1);

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
    void nullIdSerializeTest() throws JsonProcessingException {
        String badYaml = "---\n" +
           "name: \"foo\"";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        assertThrowsExactly(VirDevConfigException.class,
          () -> om.readValue(badYaml, SampleConfig.class), "property \"id\" " +
            "for node {\"name\":\"foo\"} is null.  Cannot parse any further");
    }

}
