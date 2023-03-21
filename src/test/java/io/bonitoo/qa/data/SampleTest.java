package io.bonitoo.qa.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SampleTest {

    @Test
    public void buildGenericSampleTest() throws JsonProcessingException {

        ItemConfig iConfA = new ItemNumConfig("Testak", ItemType.Double, -40, 60, 10);
        ItemConfig iConfB = new ItemNumConfig("Betaak", ItemType.Long, 0, 100, 30);
        ItemConfig builtIn = new ItemConfig("TempAk", ItemType.BuiltInTemp);
        ItemConfig iConfString = new ItemStringConfig("Stringak", ItemType.String, Arrays.asList("RED","BLUE","GREEN"));
        SampleConfig sConf = new SampleConfig("random", "fooSample", "test/items", Arrays.asList(iConfA,iConfB,builtIn,iConfString));

       // GenericSample gs = GenericSample.genSample(sConf);
        List<GenericSample> samples = new ArrayList<>();

        for(int i = 0; i < 3; i++){
            samples.add(GenericSample.of(sConf));
            System.out.println(String.format("sample: %s", samples.get(i)));
            System.out.println(samples.get(i).toJson());
        }

        for(GenericSample sample : samples){
            assertEquals(4, sample.getItems().size());
            assertTrue(sample.getItems().keySet().contains(iConfA.getName()));
            assertTrue(sample.getItems().keySet().contains(iConfB.getName()));
            assertTrue(sample.getItems().keySet().contains(iConfString.getName()));
            assertTrue(sample.getItems().keySet().contains(builtIn.getName()));
            assertTrue(sample.item("Testak") instanceof Double);
            assertTrue(sample.item("Betaak") instanceof Long);
            assertTrue(sample.item("Stringak") instanceof String);
            assertTrue(sample.item("TempAk") instanceof Double);
            assertNotEquals("random", sample.getId());
        }
    }

    /*
Example from CNT

{
  "appId": "AIR_QUAL",
  "data": 26.0,
  "messageType": "DATA",
  "ts": 1677874340000,
  "timestamp": "3/3/3333 3:33:33 PM"
}
 */
    @Test
    public void CNTExampleTest() throws JsonProcessingException {

        String nowString = ZonedDateTime.now(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_INSTANT);

        ItemConfig dataConf = new ItemNumConfig("data", ItemType.Double, 0, 100, 1);
        ItemConfig messageTypeConf = new ItemStringConfig("messageType", ItemType.String, Arrays.asList("DATA"));
        ItemConfig datestampConf = new ItemStringConfig("datestamp", ItemType.String,
                Arrays.asList(nowString));

        SampleConfig sampConf = new SampleConfig("AIR_QUAL", "CNTSample", "test/airqual", Arrays.asList(dataConf, messageTypeConf, datestampConf));

        Sample sample = GenericSample.of(sampConf);

        String sampleAsJson = sample.toJson();
        System.out.println(sample);
        System.out.println(sampleAsJson);
        ObjectMapper mapper = new ObjectMapper();

        HashMap<String,Object> map = mapper.readValue(sampleAsJson, new TypeReference<HashMap<String,Object>>(){});

        Set<String> keys = map.keySet();
        assertEquals(5, keys.size());
        assertTrue(keys.contains("id"));
        assertTrue(keys.contains("timestamp"));
        assertTrue(keys.contains("datestamp"));
        assertTrue(keys.contains("data"));
        assertTrue(keys.contains("messageType"));
        assertInstanceOf(Double.class, map.get("data"));
        assertInstanceOf(Long.class, map.get("timestamp"));
    }

    @Test
    public void noDuplicateFieldNamesTest() throws JsonProcessingException {
        ItemConfig badTsConf = new ItemStringConfig("timestamp", ItemType.String, Arrays.asList("SHOULD NOT APPEAR"));
        ItemConfig badTopicConf = new ItemNumConfig("topic", ItemType.Long,0, 0, 1 );
        ItemConfig badIdConf = new ItemNumConfig("id", ItemType.Long,0, 0, 1 );

        ItemConfig okConf = new ItemNumConfig("data", ItemType.Double, 0, 100, 1);

        SampleConfig sampConf = new SampleConfig("Test Sample", "fooSample", "test/sample",
                Arrays.asList(badTsConf,badTopicConf,badIdConf,okConf));

        Sample sample = GenericSample.of(sampConf);

        String sampleAsJson = sample.toJson();

        System.out.println(sampleAsJson);

        ObjectMapper mapper = new ObjectMapper();

        HashMap<String,Object> map = mapper.readValue(sampleAsJson, new TypeReference<HashMap<String,Object>>(){});

        assertInstanceOf(Long.class, map.get("timestamp"));
        assertInstanceOf(String.class,map.get("id"));
        assertNull(map.get("topic"));
    }

    @Test
    public void getInexistantSampleConf(){

        assertThrowsExactly(java.lang.RuntimeException.class,
                () -> SampleConfigRegistry.get("abcdefghijklmnopqrstuvwxyz"),
                "Sample Configuration named abcdefghijklmnopqrstuvwxyz not found");

    }
}
