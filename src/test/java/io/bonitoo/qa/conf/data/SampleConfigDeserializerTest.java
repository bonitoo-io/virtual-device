package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import io.bonitoo.qa.data.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleConfigDeserializerTest {

    static ItemConfig confDouble = new ItemNumConfig("doubleConf", ItemType.Double, -5, 10, 1);
    static ItemConfig confLong = new ItemNumConfig("longConf", ItemType.Long, 0, 100, 1);

    static ItemConfig confString = new ItemStringConfig("stringConf", ItemType.String, Arrays.asList("Pepe", "Lance", "Bongo"));

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
}
