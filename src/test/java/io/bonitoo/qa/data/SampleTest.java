package io.bonitoo.qa.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bonitoo.qa.conf.data.*;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.data.generator.SimpleStringGenerator;
import io.bonitoo.qa.plugin.eg.CounterItemPlugin;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.PluginResultType;
import io.bonitoo.qa.plugin.PluginType;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class SampleTest {

    @Test
    public void buildGenericSampleTest() throws JsonProcessingException {

        ItemConfig iConfA = new ItemNumConfig("Testak", "test", ItemType.Double, -40, 60, 10.0, NumGenerator.DEFAULT_DEV);
        ItemConfig iConfB = new ItemNumConfig("Betaak", "beta", ItemType.Long, 0, 100, 30.0, NumGenerator.DEFAULT_DEV);
        ItemConfig iConfString = new ItemStringConfig("Stringak", "colod", ItemType.String, Arrays.asList("RED","BLUE","GREEN"));
        SampleConfig sConf = new SampleConfig("random", "fooSample", "test/items", Arrays.asList(iConfA,iConfB,iConfString));

       // GenericSample gs = GenericSample.genSample(sConf);
        List<GenericSample> samples = new ArrayList<>();

        for(int i = 0; i < 3; i++){
            samples.add(GenericSample.of(sConf));
            System.out.printf("sample: %s%n", samples.get(i));
            System.out.println(samples.get(i).toJson());
        }

        for(GenericSample sample : samples){
            assertEquals(3, sample.getItems().size());
            assertTrue(sample.getItems().containsKey(iConfA.getName()));
            assertTrue(sample.getItems().containsKey(iConfB.getName()));
            assertTrue(sample.getItems().containsKey(iConfString.getName()));
            assertTrue(sample.item("Testak").getVal() instanceof Double);
            assertTrue(sample.item("Betaak").getVal() instanceof Long);
            assertTrue(sample.item("Stringak").getVal() instanceof String);
            assertNotEquals("random", sample.getId());
        }
    }

    /*
Example from Scientio

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

        ItemConfig dataConf = new ItemNumConfig("data", "foo", ItemType.Double, 0, 100, 1.0, NumGenerator.DEFAULT_DEV);
        ItemConfig messageTypeConf = new ItemStringConfig("messageType", "data", ItemType.String, Arrays.asList("DATA"));
        ItemConfig datestampConf = new ItemStringConfig("datestamp", "datestamp", ItemType.String,
                Arrays.asList(nowString));

        SampleConfig sampConf = new SampleConfig("AIR_QUAL", "CNTSample", "test/airqual", Arrays.asList(dataConf, messageTypeConf, datestampConf));

        Sample sample = GenericSample.of(sampConf);

        String sampleAsJson = sample.toJson();
        System.out.println(sample);
        System.out.println(sampleAsJson);
        ObjectMapper mapper = new ObjectMapper();

        // N.B. deserializer creates map keys based on label value not name
        HashMap<String,Object> map = mapper.readValue(sampleAsJson, new TypeReference<HashMap<String,Object>>(){});

        Set<String> keys = map.keySet();
        assertEquals(5, keys.size());
        assertTrue(keys.contains("id"));
        assertTrue(keys.contains("timestamp"));
        assertTrue(keys.contains("datestamp"));
        assertTrue(keys.contains("data"));
        assertTrue(keys.contains("foo"));
        assertInstanceOf(Double.class, map.get("foo"));
        assertInstanceOf(Long.class, map.get("timestamp"));
    }

    @Test
    public void noDuplicateFieldNamesTest() throws JsonProcessingException {
        ItemConfig badTsConf = new ItemStringConfig("timestamp", "timestamp", ItemType.String, Arrays.asList("SHOULD NOT APPEAR"));
        ItemConfig badTopicConf = new ItemNumConfig("topic", "topic", ItemType.Long,0, 0, 1.0, NumGenerator.DEFAULT_DEV );
        ItemConfig badIdConf = new ItemNumConfig("id", "id", ItemType.Long,0, 0, 1.0, NumGenerator.DEFAULT_DEV );

        ItemConfig okConf = new ItemNumConfig("data", "data", ItemType.Double, 0, 100, 1.0, NumGenerator.DEFAULT_DEV);

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

    @Test
    public void copySampleTest(){
        ItemConfig itemConfA = new ItemNumConfig("size", "size", ItemType.Double, 1, 15, 2.0, NumGenerator.DEFAULT_DEV);
        ItemConfig itemConfB = new ItemNumConfig("incidents", "inc", ItemType.Long, 0l, 20l, 1.0, NumGenerator.DEFAULT_DEV);
        ItemConfig itemConfC = new ItemStringConfig("alert", "alert", ItemType.String, Arrays.asList("OK", "INFO", "WARN", "CRIT"));

        SampleConfig origSConf = new SampleConfig("random", "testing", "test/copy",
                Arrays.asList(itemConfA, itemConfB, itemConfC));

        SampleConfig copySConf = new SampleConfig(origSConf);

        assertEquals(copySConf, origSConf);
        assertNotEquals(copySConf.hashCode(), origSConf.hashCode());
    }

    @Test
    public void samplesReuseItemEachWithOwnGenerator() throws ClassNotFoundException {

        String itemName = "counter";

        PluginProperties props = new PluginProperties(CounterItemPlugin.class.getName(),
          "testCounterPlugin", "count", "a counter", "0.0.1", PluginType.Item, PluginResultType.Long,
          new Properties());

        // N.B. GenericSample.of looks up plugins now
        ItemPluginMill.addPluginClass(props.getName(), props);

        ItemConfig dataConf = new ItemPluginConfig(props, itemName);
        SampleConfig conf1 = new SampleConfig("random", "first", "test/first",
          Collections.singletonList(dataConf));
        SampleConfig conf2 = new SampleConfig("random", "second", "test/second",
          Collections.singletonList(dataConf));

        Sample s1 = GenericSample.of(conf1);
        Sample s2 = GenericSample.of(conf2);

        ((CounterItemPlugin)s1.item(itemName).getGenerator()).onLoad(); // init the plugin instance
        ((CounterItemPlugin)s2.item(itemName).getGenerator()).onLoad(); // init the plugin instance

        assertNotEquals(s1.item(itemName).getGenerator(), s2.item(itemName).getGenerator());
        while (s1.item(itemName).asLong() < 7L){
            s1.item(itemName).update();
        }
        assertNotEquals(s1.item(itemName).asLong(),s2.item(itemName).asLong());
    }

    @Test
    public void sampleGenericUpdateTest(){
        ItemConfig itemConf = new ItemNumConfig("size", "size", ItemType.Double, 1, 15, 2.0, NumGenerator.DEFAULT_DEV);

        SampleConfig sampleConf = new SampleConfig("random", "testing", "test/copy",
          Arrays.asList(itemConf));

        GenericSample gs = GenericSample.of(sampleConf);
        long originalTs = gs.getTimestamp();
        double originalVal = gs.item("size").asDouble();
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
        gs.update();
        assertNotEquals(originalVal, gs.item("size").asDouble());
        assertTrue(originalVal < gs.getTimestamp());

    }
}
