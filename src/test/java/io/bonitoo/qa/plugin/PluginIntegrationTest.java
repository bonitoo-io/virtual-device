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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
  public void pluginLoad() throws JsonProcessingException, InterruptedException {

    File[] pluginFiles = new File("plugins/examples/accelerator").listFiles((dir, name) ->
      name.toLowerCase().endsWith(".jar")
    );
    System.out.println("DEBUG pluginFiles.length " + pluginFiles.length);
    for(File f : pluginFiles){
      System.out.println("DEBUG file " + f.getName());
      try {
        Class<ItemGenPlugin> clazz = (Class<ItemGenPlugin>) PluginLoader.loadPlugin(f);
        System.out.println("DEBUG clazz " + clazz.getName());
      } catch (IOException | PluginConfigException |
               ClassNotFoundException | NoSuchFieldException |
               IllegalAccessException e) {
        throw new VirDevConfigException(e);
      }
    }

    System.out.println("DEBUG ItemPluginMill keys " + ItemPluginMill.getKeys());

    ItemConfig itemConfig;

    // TODO add asserts

    //      itemConfig = new ItemPluginConfig("AcceleratorPlugin", "AcceleratorTest", "speed",
//        PluginResultType.Double, new HashSet<>());
    itemConfig = new ItemPluginConfig(ItemPluginMill.getPluginProps("AcceleratorPlugin"),
      "AcceleratorTest", new Vector<>());

    System.out.println("DEBUG itemConfig itemGen " + ((ItemPluginConfig)itemConfig).getGenClassName());
//      System.out.println("DEBUG itemConfig itemGen currentVal " + ((ItemPluginConfig)itemConfig).getItemGen().getCurrentVal());

    SampleConfig sConf = new SampleConfig("random", "accelTestSample", "test/accel", Arrays.asList(itemConfig));

    DeviceConfig devConf = new DeviceConfig( "random",
      "accelTestDevice",
      "testing accelerator plugin",
      Arrays.asList(sConf),
      500L,
      0L,
      1);

    ObjectWriter yamlWriter = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

    System.out.println("DEBUG deviceConfig\n" + yamlWriter.writeValueAsString(devConf));

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Config.getRunnerConfig().setDevices(Arrays.asList(devConf));

    System.out.println("DEBUG Config.runnerConfig\n" + yamlWriter.writeValueAsString(Config.getRunnerConfig()));

    GenericDevice accelDevice = GenericDevice.singleDevice(mockClient, Config.deviceConf(0));

    executor.execute(accelDevice);

    executor.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);

    executor.shutdown();
/*
    verify(mockClient, times(1)).connect();

    for (SampleConfig sampConf : Config.getSampleConfs(0)) {
      verify(mockClient, times(20)).publish(eq(sampConf.getTopic()), anyString());
    }

   */

  }
}
