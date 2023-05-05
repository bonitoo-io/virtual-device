package io.bonitoo.qa.conf.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.device.DeviceConfigDeserializer;
import io.bonitoo.qa.data.*;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeviceConfigDeserializerTest {

    static SampleConfig testSampConfig = new SampleConfig("abcd","testSample","test/parsing",
            Arrays.asList(new ItemNumConfig("tension", "tns", ItemType.Double, -1, 2, 1),
                    new ItemNumConfig("nuts", "nuts", ItemType.Long, 1, 100, 1),
                    new ItemStringConfig("label", "lbl", ItemType.String, Arrays.asList("Salted","unsalted","smoked"))));
    static DeviceConfig testDevConfig = new DeviceConfig("1234",
                            "Test Device",
                            "A device for testing",
                            Arrays.asList(testSampConfig),
                              1000l,
                              0l,
                              1);

    static String JSONDevConfig;
    static String YAMLDevConfig;

    @BeforeAll
    public static void genConfigs() throws JsonProcessingException {
        ObjectWriter jsonWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectWriter yamlWriter = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

        JSONDevConfig = jsonWriter.writeValueAsString(testDevConfig);
        YAMLDevConfig = yamlWriter.writeValueAsString(testDevConfig);
    }

    @Test
    public void baseJSONTest() throws JsonProcessingException {

        ObjectMapper om = new ObjectMapper();
        DeviceConfig testCfg = om.readValue(JSONDevConfig, DeviceConfig.class);

        assertEquals(testDevConfig, testCfg);
    }

    @Test
    public void baseYAMLTest() throws JsonProcessingException {

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        DeviceConfig testCfg = om.readValue(YAMLDevConfig, DeviceConfig.class);

        assertEquals(testDevConfig, testCfg);
    }

    @Test
    public void yamlTestWithKeyStrings() throws JsonProcessingException {

        ItemConfig ic1 = new ItemNumConfig("alpha", "alpha", ItemType.Double, -100, 100, 1);
        ItemConfig ic2 = new ItemNumConfig("beta", "beta", ItemType.Long, 0, 1000, 2);
        ItemConfig ic3 = new ItemStringConfig("gamma", "gamma", ItemType.String, Arrays.asList("Sirius", "Canopus", "Arcturus"));
        ItemConfig ic4 = new ItemStringConfig("delta", "delta", ItemType.String, Arrays.asList("Everest", "K2", "Kangchenjunga"));
        ItemConfig ic5 = new ItemNumConfig("epsilon", "epsl", ItemType.Double, -1, 10000, 3);

        SampleConfig sc1 = new SampleConfig("random", "first", "testing/first" , new String[]{ ic1.getName(), ic3.getName() });
        SampleConfig sc2 = new SampleConfig("random", "second", "testing/second", Arrays.asList(ic2, ic4));
        SampleConfig sc3 = new SampleConfig("random","third", "testing/third", Arrays.asList(ic5));

        String id = "1234";
        String name = "TestDevice";
        String description = "A device for testing";
        Long interval = 1000l;
        Long jitter = 0l;
        int count = 1;

        String deviceConfYAML = "---\n" +
                "id: \"" + id + "\"\n" +
                "name: \"" + name + "\"\n" +
                "description: \"" + description + "\" \n" +
                "interval: " + interval + "\n" +
                "jitter: " + jitter + "\n" +
                "count: " + count + "\n" +
                "samples:\n" +
                "- " + sc1.getName() + "\n" +
                "- " + sc2.getName() + "\n" +
                "- " + sc3.getName();

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        DeviceConfig conf = om.readValue(deviceConfYAML, DeviceConfig.class);

        assertEquals(id, conf.getId());
        assertEquals(name, conf.getName());
        assertEquals(description, conf.getDescription());
        assertEquals(interval, conf.getInterval());
        assertEquals(jitter, conf.getJitter());
        assertEquals(count, conf.getCount());

        assertEquals(3, conf.getSamples().size());
        assertEquals(sc1, conf.getSample(sc1.getName()));
        assertEquals(sc2, conf.getSample(sc2.getName()));
        assertEquals(sc3, conf.getSample(sc3.getName()));
    }

    @Test
    public void yamlTestMissingValues() throws JsonProcessingException {

        ItemConfig ic1 = new ItemNumConfig("alpha", "alpha", ItemType.Double, -100, 100, 1);
        ItemConfig ic2 = new ItemNumConfig("beta", "beta", ItemType.Long, 0, 1000, 2);

        SampleConfig sc1 = new SampleConfig("random", "first", "testing/first" , new String[]{ ic1.getName(), ic2.getName() });

        String id = "1234";
        String name = "TestDevice";
        String description = "A device for testing";

        String deviceConfYAML = "---\n" +
                "id: \"" + id + "\"\n" +
                "name: \"" + name + "\"\n" +
                "description: \"" + description + "\" \n" +
                "samples:\n" +
                "- " + sc1.getName();

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        DeviceConfig conf = om.readValue(deviceConfYAML, DeviceConfig.class);

        assertEquals(id, conf.getId());
        assertEquals(name, conf.getName());
        assertEquals(description, conf.getDescription());
        assertEquals(id, conf.getId());
        assertEquals(sc1, conf.getSamples().get(0));

        assertEquals(DeviceConfigDeserializer.defaultInterval, conf.getInterval());
        assertEquals(DeviceConfigDeserializer.defaultJitter, conf.getJitter());
        assertEquals(DeviceConfigDeserializer.defaultCount, conf.getCount());
    }


}
