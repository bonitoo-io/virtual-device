package io.bonitoo.qa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.Mode;
import io.bonitoo.qa.conf.RunnerConfig;
import io.bonitoo.qa.conf.RunnerConfigDeserializer;
import io.bonitoo.qa.conf.data.*;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.AuthConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.device.Device;
import io.bonitoo.qa.device.TestDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
public class DeviceRunnerTest {

    @BeforeEach
    public void setup() throws InterruptedException {
        Config.reset(); // NB reads default.conf which adds a sample to registry
        SampleConfigRegistry.clear();
    }

    @Test
    @Disabled("runSingleDeviceTest is at device.DeviceTest.genericDeviceBaseTest")
    public void runSingleDeviceTest(){

    }

    @Test
    @Disabled("runThreeDevicesTest is at device.DeviceTest.genericDeviceThreeSampleTest() et al.")
    public void runThreeDevicesTest(){

    }

    @Test
    public void testRunnerConfigYaml() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File confFile = new File(loader.getResource("testRunnerConfig.yml").getFile());

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        RunnerConfig conf = om.readValue(confFile, RunnerConfig.class);

        assertEquals(new BrokerConfig("localhost", 1883, new AuthConfig("fred", "changeit".toCharArray())),
                conf.getBroker());

        assertEquals(2, conf.getDevices().size());
        assertEquals(3, SampleConfigRegistry.keys().size());
        assertEquals(6, ItemConfigRegistry.keys().size());

    }

    @Test
    public void runnerConfigReflectionTest() throws JsonProcessingException {

        ItemConfig itemA = new ItemNumConfig("alligator", "all", ItemType.Double, -10, 10, 2.0, NumGenerator.DEFAULT_DEV);
        ItemConfig itemB = new ItemNumConfig("baboon", "bab", ItemType.Long, -50, 50, 1.0, NumGenerator.DEFAULT_DEV);
        ItemConfig itemC = new ItemStringConfig("cockatoo", "too", ItemType.String, Arrays.asList("un", "deux", "trois"));
        ItemConfig itemD = new ItemNumConfig("dingo", "din", ItemType.Double, 0, 60, 4.0, NumGenerator.DEFAULT_DEV);

        SampleConfig sample01 = new SampleConfig("random", "elephant", "test/elephant",
                Arrays.asList(itemA, itemB, itemC, itemD));
        SampleConfig sample02 = new SampleConfig("random", "fox", "test/fox",
                new String[]{itemA.getName(), itemB.getName(), itemC.getName(), itemD.getName()});

        DeviceConfig device = new DeviceConfig("random", "giraffe", "A test giraffe test device",
                Arrays.asList(sample01, sample02), 1000l, 0l, 1);

        BrokerConfig broker = new BrokerConfig("my.mqttserver.net", 1883, new AuthConfig("fred", "changeit".toCharArray()));

        RunnerConfig runnerConf = new RunnerConfig(broker, Arrays.asList(device), 30000l, Mode.BLOCKING);

        ObjectWriter writer = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

        String confAsYaml = writer.writeValueAsString(runnerConf);

        System.out.println("DEBUG confAsYaml:\n" + confAsYaml);

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        RunnerConfig parsedConf = om.readValue(confAsYaml, RunnerConfig.class);

        assertEquals(runnerConf.getTtl(), parsedConf.getTtl());
        assertEquals(runnerConf.getBroker(), parsedConf.getBroker());
        assertEquals(runnerConf.getMode(), parsedConf.getMode());

        for(DeviceConfig deviceConf: runnerConf.getDevices()){
            assertTrue(parsedConf.getDevices().contains(deviceConf));
        }
        for(DeviceConfig deviceConf: parsedConf.getDevices()){
            assertTrue(runnerConf.getDevices().contains(deviceConf));
        }
    }

    @Test
    public void checkRunnerReactiveBranch() throws InterruptedException {

        List<Device> tds = Arrays.asList(new TestDevice(), new TestDevice(), new TestDevice());

        DeviceRunner.reactiveMain(tds);

        Thread.sleep(1000L);

        for(Device dev : tds){
            assertTrue(((TestDevice)dev).isCalled());
        }
    }

}
