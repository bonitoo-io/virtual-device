package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.device.GenericDevice;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.plugin.item.ItemGenPlugin;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import io.bonitoo.qa.plugin.sample.SamplePlugin;
import io.bonitoo.qa.plugin.sample.SamplePluginMill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("intg")
@ExtendWith(MockitoExtension.class)
public class PluginIntegrationTest {

  @Mock
  MqttClientBlocking mockClient;

  @BeforeEach
  public void setup() throws InterruptedException {
    reset(mockClient);
    lenient().when(mockClient.connect()).thenReturn(mockClient);
    Config.reset();
  }

  @Test
  public void loadItemPlugin() throws JsonProcessingException, InterruptedException {

    final String accelClassName = "io.bonitoo.virdev.plugin.AcceleratorPlugin";

    File[] pluginFiles = new File("plugins/examples/accelerator").listFiles((dir, name) ->
      name.toLowerCase().endsWith(".jar")
    );
    assertNotNull(pluginFiles);
    assertEquals(1, pluginFiles.length);
    for(File f : pluginFiles){
      assertEquals("accelerator-0.1-SNAPSHOT.jar", f.getName());
      try {
        @SuppressWarnings("unchecked")
        Class<ItemGenPlugin> clazz = (Class<ItemGenPlugin>) PluginLoader.loadPlugin(f);
        assertNotNull(clazz);
        assertEquals(accelClassName, clazz.getName());
      } catch (IOException | PluginConfigException |
               ClassNotFoundException | NoSuchFieldException |
               IllegalAccessException e) {
        throw new VirDevConfigException(e);
      }
    }

    assertTrue(ItemPluginMill.getKeys().contains("AcceleratorPlugin"));

    ItemConfig itemConfig;

    itemConfig = new ItemPluginConfig(ItemPluginMill.getPluginProps("AcceleratorPlugin"),
      "AcceleratorTest");

    assertEquals(accelClassName, itemConfig.getGenClassName());
    assertEquals("AcceleratorTest", itemConfig.getName());
    assertEquals("speed", itemConfig.getLabel());

    SampleConfig sConf = new SampleConfig("random", "accelTestSample", "test/accel", Arrays.asList(itemConfig));

    DeviceConfig devConf = new DeviceConfig( "random",
      "accelTestDevice",
      "testing accelerator plugin",
      Collections.singletonList(sConf),
      500L,
      0L,
      1);

    ObjectWriter yamlWriter = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Config.getRunnerConfig().setDevices(Collections.singletonList(devConf));

    GenericDevice accelDevice = GenericDevice.singleDevice(mockClient, Config.deviceConf(0));

    executor.execute(accelDevice);

    executor.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);

    executor.shutdown();

    verify(mockClient, times(1)).connect();

    for (SampleConfig sampConf : Config.getSampleConfs(0)) {
      verify(mockClient, times(20)).publish(eq(sampConf.getTopic()), anyString());
    }

  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  static class LineProtocol {
    String Measurement;
    Map<String,String> tags;
    Map<String,Object> fields;
    Long timestamp;
  }

  @Test
  public void loadSamplePlugin() throws JsonProcessingException, InterruptedException {

    final String pluginClassName = "io.bonitoo.virdev.plugin.LPFileReaderPlugin";

    File[] pluginFiles = new File("plugins/examples/lpFileReader").listFiles((dir, name) ->
      name.toLowerCase().endsWith(".jar")
    );

    assertNotNull(pluginFiles);
    assertEquals(1, pluginFiles.length);

    for(File f : pluginFiles){
      assertEquals("lpFileReader-0.1-SNAPSHOT.jar", f.getName());
      try {
        @SuppressWarnings("unchecked")
        Class<SamplePlugin> clazz = (Class<SamplePlugin>) PluginLoader.loadPlugin(f);
        assertNotNull(clazz);
        assertEquals(pluginClassName, clazz.getName());
      } catch (IOException | PluginConfigException |
               ClassNotFoundException | NoSuchFieldException |
               IllegalAccessException e) {
        throw new VirDevConfigException(e);
      }
    }

    assertTrue(SamplePluginMill.getKeys().contains("LPFileReader"));

    Class<?> clazz = SamplePluginMill.getPluginClass("LPFileReader");
    assertEquals(pluginClassName, clazz.getName());

    // N.B. - the extended SamplePlugin class and extended SamplePluginConfig class
    // cannot always be known to the device runner.  However, the DeviceConfig deserializer
    // is designed to detect the extended config from the SamplePluginConfigClass annotation.
    // So to test the "anonymous" configuration, use here a device configuration, that includes
    // a YAML configuration entry for the extended SamplePluginConfig.

    String deviceConfYaml = "---\n" +
      "  id: \"random\"\n" +
      "  name: \"Test Device\"\n" +
      "  description: \"test device configuration with plugin\"\n" +
      "  interval: 300\n" +
      "  jitter: 0\n" +
      "  count: 1\n" +
      "  samples:\n" +
      "    - id: \"random\"\n" +
      "      name: \"LPFileReaderConf\"\n" +
      "      topic: \"test/linep\"\n" +
      "      items: []\n" + // N.B. even though items will be ignored, the field is required.
      "      plugin: \"LPFileReader\"\n" +
      "      source: \"./plugins/examples/lpFileReader/data/myTestLP.lp\"";

    ObjectMapper omy = new ObjectMapper(new YAMLFactory());

    DeviceConfig devConf = omy.readValue(deviceConfYaml, DeviceConfig.class);

    Config.getRunnerConfig().setDevices(Collections.singletonList(devConf));

    GenericDevice lpSampleDevice = GenericDevice.singleDevice(mockClient, Config.deviceConf(0));

    String jsonSample = lpSampleDevice.getSampleList().get(0).toJson();
    ObjectMapper omj = new ObjectMapper();

    // Sample first value - is truly LineProtocol
    LineProtocol lp = omj.readValue(jsonSample, LineProtocol.class);
    assertEquals("windgen", lp.getMeasurement());
    long tenSecsAgo = System.currentTimeMillis() - 10000;
    assertTrue(lp.getTimestamp() > tenSecsAgo && lp.getTimestamp() < System.currentTimeMillis());
    assertEquals("R20WT01", lp.getTags().get("turbine"));
    assertEquals("A02", lp.getTags().get("unit"));
    assertEquals("VRCH03", lp.getTags().get("group"));
    assertEquals(9.9, lp.getFields().get("windspeed"));
    assertEquals(192.81, lp.getFields().get("kw"));
    assertEquals(89.87, lp.getFields().get("direction"));


    // Now try and run it
    ExecutorService executor = Executors.newSingleThreadExecutor();

    executor.execute(lpSampleDevice);

    executor.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);

    executor.shutdown();

    verify(mockClient, times(1)).connect();

    // N.B. there are 32 samples in the test file, so need to run more
    // than 32 updates to ensure the LPFileReader plugin cycles back to
    // the first record

    for (SampleConfig sampConf : Config.getSampleConfs(0)) {
      verify(mockClient, times(34)).publish(eq(sampConf.getTopic()), anyString());
    }
  }

  /*
  public static class FooItemPluginConfig extends ItemPluginConfig {

    public FooItemPluginConfig(PluginProperties props, String name) {
      super(props, name);
    }

    public FooItemPluginConfig(ItemPluginConfig config) {
      super(config);
    }
  }

   */

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FooSample extends GenericSample {
    double foo;
  }


  @Test
  @Tag("intg")
  public void loadComplexItemPlugin()
    throws PluginConfigException, InvocationTargetException, NoSuchMethodException,
    InstantiationException, IllegalAccessException, JsonProcessingException, InterruptedException {

    // TODO review in light of new List<Item> structure and Item arrays API

    final String simpleMovingAvgClassName = "io.bonitoo.virdev.plugin.SimpleMovingAvg";
    final String simpleMovingAvgName = "SimpleMovingAvg";
    final String sampleID = "fooSample2050";
    final double min = 0.0;
    final double max = 99.9;
    final int prec = 3;

    File[] pluginFiles = new File("plugins/examples/simpleMovingAvg").listFiles((dir, name) ->
      name.toLowerCase().endsWith(".jar")
    );
    assertNotNull(pluginFiles);
    assertEquals(1, pluginFiles.length);
    for(File f : pluginFiles){
      assertEquals("simpleMovingAverage-0.1-SNAPSHOT.jar", f.getName());
      try {
        @SuppressWarnings("unchecked")
        Class<ItemGenPlugin> clazz = (Class<ItemGenPlugin>) PluginLoader.loadPlugin(f);
        assertNotNull(clazz);
        assertEquals(simpleMovingAvgClassName, clazz.getName());
      } catch (IOException | PluginConfigException |
               ClassNotFoundException | NoSuchFieldException |
               IllegalAccessException e) {
        throw new VirDevConfigException(e);
      }
    }

    assertTrue(ItemPluginMill.getKeys().contains(simpleMovingAvgName));

    Class<?> clazz = ItemPluginMill.getPluginClass(simpleMovingAvgName);
    assertEquals(simpleMovingAvgClassName, clazz.getName());

    String deviceTestConfig = "---\n" +
      "id: \"random\"\n" +
      "name: \"simpleMovingAvgTestDevice\"\n" +
      "description: \"testing simpleMovingAvg plugin\"\n" +
      "samples:\n" +
      "- name: \"simpleMovingAvgTestSample\"\n" +
      "  id: \"" + sampleID + "\"\n" +
      "  topic: \"test/foo\"\n" +
      "  items:\n" +
      "  - name: \"simpleMovingAvgConf\"\n" +
      "    label: \"foo\"\n" +
      "    type: \"Plugin\"\n" +
      "    pluginName: \"SimpleMovingAvg\"\n" +
      "    resultType: \"Double\"\n" +
      "    prec: " + prec +  "\n" +
      "    window: 5\n" +
      "    min: " + min + "\n" +
      "    max: " + max + "\n" +
      "interval: 500\n" +
      "jitter: 100\n" +
      "count: 1";

    ObjectMapper omy = new ObjectMapper(new YAMLFactory());

    DeviceConfig devConf = omy.readValue(deviceTestConfig, DeviceConfig.class);

    Config.getRunnerConfig().setDevices(Collections.singletonList(devConf));

    GenericDevice avgTestDevice = GenericDevice.singleDevice(mockClient, Config.deviceConf(0));

    ObjectMapper omj = new ObjectMapper();

    FooSample fsStart = omj.readValue(avgTestDevice.getSampleList().get(0).toJson(), FooSample.class);

    assertEquals(sampleID, fsStart.getId());
    long minuteAgo = System.currentTimeMillis() - 60000;
    assertTrue(fsStart.getTimestamp() > minuteAgo && fsStart.getTimestamp() < System.currentTimeMillis());
    assertInstanceOf(Double.class, fsStart.getFoo());
    assertTrue(fsStart.getFoo() >= min && fsStart.getFoo() <= max);

    ObjectWriter ow = omj.writer();
    String valPrec = ow.writeValueAsString(fsStart.getFoo());
    assertTrue(valPrec.split("\\.")[1].length() <= prec);

    ExecutorService executor = Executors.newSingleThreadExecutor();

    executor.execute(avgTestDevice);

    executor.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);

    executor.shutdown();

    FooSample fsEnd = omj.readValue(avgTestDevice.getSampleList().get(0).toJson(), FooSample.class);

    assertEquals(sampleID, fsEnd.getId());
    minuteAgo = System.currentTimeMillis() - 60000;
    assertTrue(fsEnd.getTimestamp() > minuteAgo && fsEnd.getTimestamp() < System.currentTimeMillis());
    assertInstanceOf(Double.class, fsEnd.getFoo());
    assertTrue(fsEnd.getFoo() >= min && fsEnd.getFoo() <= max);
    assertNotEquals(fsStart.getFoo(), fsEnd.getFoo());

    valPrec = ow.writeValueAsString(fsEnd.getFoo());
    assertTrue(valPrec.split("\\.")[1].length() <= prec);


    verify(mockClient, times(1)).connect();

    SampleConfig sampConf = Config.getSampleConfs(0).get(0);
    verify(mockClient, times(17)).publish(eq(sampConf.getTopic()), anyString());

  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FooArraySample extends GenericSample {
    double foo00;
    double foo01;
    double foo02;
  }

  @Test
  @Tag("intg")
  public void itemPluginWithMultipleItemsBasic() throws JsonProcessingException {

    final String simpleMovingAvgClassName = "io.bonitoo.virdev.plugin.SimpleMovingAvg";
    final String simpleMovingAvgName = "SimpleMovingAvg";
    final String sampleID = "fooSample2050";
    final double min = 0.0;
    final double max = 99.9;
    final int prec = 3;

    File[] pluginFiles = new File("plugins/examples/simpleMovingAvg").listFiles((dir, name) ->
      name.toLowerCase().endsWith(".jar")
    );
    assertNotNull(pluginFiles);
    assertEquals(1, pluginFiles.length);

    for(File f : pluginFiles){
      assertEquals("simpleMovingAverage-0.1-SNAPSHOT.jar", f.getName());
      try {
        @SuppressWarnings("unchecked")
        Class<ItemGenPlugin> clazz = (Class<ItemGenPlugin>) PluginLoader.loadPlugin(f);
        assertNotNull(clazz);
        assertEquals(simpleMovingAvgClassName, clazz.getName());
      } catch (IOException | PluginConfigException |
               ClassNotFoundException | NoSuchFieldException |
               IllegalAccessException e) {
        throw new VirDevConfigException(e);
      }
    }

    assertTrue(ItemPluginMill.getKeys().contains(simpleMovingAvgName));

    Class<?> clazz = ItemPluginMill.getPluginClass(simpleMovingAvgName);
    assertEquals(simpleMovingAvgClassName, clazz.getName());

    String deviceTestConfig = "---\n" +
      "id: \"random\"\n" +
      "name: \"simpleMovingAvgTestDevice\"\n" +
      "description: \"testing simpleMovingAvg plugin\"\n" +
      "samples:\n" +
      "- name: \"simpleMovingAvgTestSample\"\n" +
      "  id: \"" + sampleID + "\"\n" +
      "  topic: \"test/foo\"\n" +
      "  items:\n" +
      "  - name: \"simpleMovingAvgConf\"\n" +
      "    label: \"foo\"\n" +
      "    type: \"Plugin\"\n" +
      "    count: 3\n" +
      "    pluginName: \"SimpleMovingAvg\"\n" +
      "    resultType: \"Double\"\n" +
      "    prec: " + prec +  "\n" +
      "    window: 5\n" +
      "    min: " + min + "\n" +
      "    max: " + max + "\n" +
      "interval: 500\n" +
      "jitter: 100\n" +
      "count: 1";

    ObjectMapper omy = new ObjectMapper(new YAMLFactory());

    DeviceConfig devConf = omy.readValue(deviceTestConfig, DeviceConfig.class);

//    System.out.println("DEBUG devConf.getSamples() " + devConf.getSamples().size());
//    System.out.println("DEBUG devConf.getSamples(0) " + devConf.getSamples().get(0).getItems().size());
//    System.out.println("DEBUG devConf.getSamples(0).toString " + devConf.getSamples().get(0));

    Config.getRunnerConfig().setDevices(Collections.singletonList(devConf));

    GenericDevice avgTestDevice = GenericDevice.singleDevice(mockClient, Config.deviceConf(0));

    ObjectMapper omj = new ObjectMapper();

//    System.out.println("DEBUG avgTestDevice.getSampleList() " + avgTestDevice.getSampleList().size());
//    for(Sample s : avgTestDevice.getSampleList()){
//      System.out.println("    DEBUG sample " + s.getId() + "  " + s.getTopic());
//    }

//    System.out.println("DEBUG sampleList.get(0).toJson \n" + avgTestDevice.getSampleList().get(0).toJson());
    // N.B. no longer matches Foo Sample

    FooArraySample fasStart = omj.readValue(avgTestDevice.getSampleList().get(0).toJson(), FooArraySample.class);

  }
}
