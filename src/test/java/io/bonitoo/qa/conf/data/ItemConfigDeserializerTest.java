package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.VDevConfigException;
import io.bonitoo.qa.data.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ItemConfigDeserializerTest {

    static ItemConfig confDouble = new ItemNumConfig("doubleConf", ItemType.Double, -5, 10, 1);
    static ItemConfig confLong = new ItemNumConfig("longConf", ItemType.Long, 0, 100, 1);

    static ItemConfig confString = new ItemStringConfig("stringConf", ItemType.String, Arrays.asList("Pepe", "Lance", "Bongo"));

    static String confDoubleJson;
    static String confLongJson;
    static String confStringJson;

    static String confDoubleYaml;
    static String confLongYaml;
    static String confStringYaml;

    @BeforeAll
    public static void setStringConstants() throws JsonProcessingException {
        ItemConfigRegistry.clear();

        ObjectWriter jsonWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectWriter yamlWriter = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

        confDoubleJson = jsonWriter.writeValueAsString(confDouble);
        confLongJson = jsonWriter.writeValueAsString(confLong);
        confStringJson = jsonWriter.writeValueAsString(confString);

        confDoubleYaml = yamlWriter.writeValueAsString(confDouble);
        confLongYaml = yamlWriter.writeValueAsString(confLong);
        confStringYaml = yamlWriter.writeValueAsString(confString);
    }

    @Test
    public void jsonDeserializeTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();

        ItemNumConfig configD = (ItemNumConfig)om.readValue(confDoubleJson, ItemConfig.class);
        ItemNumConfig configL = (ItemNumConfig)om.readValue(confLongJson, ItemConfig.class);
        ItemStringConfig configS = (ItemStringConfig)om.readValue(confStringJson, ItemConfig.class);

        assertEquals(configD, confDouble);
        assertEquals(configL, confLong);
        assertEquals(configS, confString);

        assertEquals(configD, ItemConfigRegistry.get(confDouble.getName()));
        assertEquals(configL, ItemConfigRegistry.get(confLong.getName()));
        assertEquals(configS, ItemConfigRegistry.get(confString.getName()));
    }

    @Test
    public void yamlDeserializerTest() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        ItemNumConfig configD = (ItemNumConfig)om.readValue(confDoubleYaml,ItemConfig.class);
        ItemNumConfig configL = (ItemNumConfig)om.readValue(confLongYaml, ItemConfig.class);
        ItemStringConfig configS = (ItemStringConfig) om.readValue(confStringYaml, ItemConfig.class);

        assertEquals(configD, confDouble);
        assertEquals(configL, confLong);
        assertEquals(configS, confString);

        assertEquals(configD, ItemConfigRegistry.get(confDouble.getName()));
        assertEquals(configL, ItemConfigRegistry.get(confLong.getName()));
        assertEquals(configS, ItemConfigRegistry.get(confString.getName()));
    }

    @Test
    public void nullPropertyDeserializeTest() throws JsonProcessingException {
        String badYaml = "---\n" +
          "name: \"profNode01\"\n" +
          "type: \"Double\"\n" +
          "max: 150\n" +
          "min: 0\n";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        assertThrowsExactly(VDevConfigException.class,
          () -> om.readValue(badYaml, ItemConfig.class),
          "property \"period\" for node {\"name\":\"profNode01\"," +
            "\"type\":\"Double\",\"max\":150,\"min\":0} is null.  Cannot parse any further");

    }


}
