package io.bonitoo.qa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.RunnerConfig;
import io.bonitoo.qa.conf.data.*;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.AuthConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.conf.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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

        ItemConfig itemA = new ItemNumConfig("alligator", "all", ItemType.Double, -10, 10, 2);
        ItemConfig itemB = new ItemNumConfig("baboon", "bab", ItemType.Long, -50, 50, 1);
        ItemConfig itemC = new ItemStringConfig("cockatoo", "too", ItemType.String, Arrays.asList("un", "deux", "trois"));
        ItemConfig itemD = new ItemNumConfig("dingo", "din", ItemType.Double, 0, 60, 4);

        SampleConfig sample01 = new SampleConfig("random", "elephant", "test/elephant",
                Arrays.asList(itemA, itemB, itemC, itemD));
        SampleConfig sample02 = new SampleConfig("random", "fox", "test/fox",
                new String[]{itemA.getName(), itemB.getName(), itemC.getName(), itemD.getName()});

        DeviceConfig device = new DeviceConfig("random", "giraffe", "A test giraffe test device",
                Arrays.asList(sample01, sample02), 1000l, 0l, 1);

        BrokerConfig broker = new BrokerConfig("my.mqttserver.net", 1883, new AuthConfig("fred", "changeit".toCharArray()));

        RunnerConfig runnerConf = new RunnerConfig(broker, Arrays.asList(device), 30000l);

        ObjectWriter writer = new ObjectMapper(new YAMLFactory()).writer().withDefaultPrettyPrinter();

        String confAsYaml = writer.writeValueAsString(runnerConf);

        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        RunnerConfig parsedConf = om.readValue(confAsYaml, RunnerConfig.class);

        assertEquals(runnerConf.getTtl(), parsedConf.getTtl());
        assertEquals(runnerConf.getBroker(), parsedConf.getBroker());

        for(DeviceConfig deviceConf: runnerConf.getDevices()){
            assertTrue(parsedConf.getDevices().contains(deviceConf));
        }
        for(DeviceConfig deviceConf: parsedConf.getDevices()){
            assertTrue(runnerConf.getDevices().contains(deviceConf));
        }

    }
}
