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
import io.bonitoo.qa.device.GenericDevice;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

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
      "AcceleratorTest", new Vector<>());

  //  System.out.println("DEBUG itemConfig itemGen " + ((ItemPluginConfig)itemConfig).getGenClassName());
    assertEquals(accelClassName, itemConfig.getGenClassName());
    assertEquals("AcceleratorTest", itemConfig.getName());
    assertEquals("speed", itemConfig.getLabel());
    assertEquals(new Vector<>(), itemConfig.getUpdateArgs());

    SampleConfig sConf = new SampleConfig("random", "accelTestSample", "test/accel", Arrays.asList(itemConfig));

    DeviceConfig devConf = new DeviceConfig( "random",
      "accelTestDevice",
      "testing accelerator plugin",
      Collections.singletonList(sConf),
      500L,
      0L,
      1);

    ObjectWriter yamlWriter = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

  //  System.out.println("DEBUG deviceConfig\n" + yamlWriter.writeValueAsString(devConf));

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Config.getRunnerConfig().setDevices(Collections.singletonList(devConf));

 //   System.out.println("DEBUG Config.runnerConfig\n" + yamlWriter.writeValueAsString(Config.getRunnerConfig()));

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
}
