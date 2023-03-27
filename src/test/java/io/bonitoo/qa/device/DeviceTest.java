package io.bonitoo.qa.device;

import io.bonitoo.qa.data.*;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.conf.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceTest {

    @Mock
    MqttClientBlocking mockClient;

    @BeforeEach
    public void setup() throws InterruptedException {
        reset(mockClient);
        lenient().when(mockClient.connect()).thenReturn(mockClient);
        Config.reset();
    }

    @Test
    public void genericDeviceBaseTest() throws InterruptedException {

        ItemConfig iConf = new ItemNumConfig("testItem", ItemType.Double, 0, 100, 1);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Config.getRunnerConfig().sampleConf(0, 0).setItems(Arrays.asList(iConf));
        Config.getRunnerConfig().setTtl(2000l);

        assertEquals(2, Config.getDeviceConfs().size());
        assertEquals(2, Config.getSampleConfs(0).size());

        DeviceConfig devConf = Config.deviceConf(0);

        SampleConfig sampConf = Config.sampleConf(0, 0);
        when(mockClient.publish(eq(sampConf.getTopic()),anyString())).thenReturn(mockClient);

        devConf.setInterval(1000l);

        GenericDevice device = GenericDevice.SingleDevice(mockClient, devConf);

        executor.execute(device);

        executor.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);

        executor.shutdown();

        verify(mockClient, times(1)).connect();
        verify(mockClient, times(2)).publish(eq(sampConf.getTopic()), anyString());
    }

    @Test
    public void genericDeviceThreeSampleTest() throws InterruptedException {

        ItemConfig iConf = new ItemNumConfig("testItem", ItemType.Double, 0, 100, 1);

        assertEquals(2, Config.getSampleConfs(0).size());
        Config.sampleConf(0, 0).setTopic("test/sample0");
        Config.sampleConf(0, 0).setId("sample0");
        Config.sampleConf(0, 0).setItems(Arrays.asList(iConf));

        Config.deviceConf(0).setSamples(new ArrayList<>());

        Config.getSampleConfs(0).add(new SampleConfig("sampleA","sampleA","test/sampleA",Arrays.asList(iConf)));
        Config.getSampleConfs(0).add(new SampleConfig("sampleB","sampleB","test/sampleB",Arrays.asList(iConf)));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Config.getRunnerConfig().setTtl(2000l);
        Config.deviceConf(0).setInterval(1000l);

        assertEquals(2, Config.getDeviceConfs().size());
        assertEquals(2, Config.getSampleConfs(0).size());

        for(SampleConfig sampConf : Config.getSampleConfs(0)) {
            when(mockClient.publish(eq(sampConf.getTopic()), anyString())).thenReturn(mockClient);
        }

        GenericDevice device = GenericDevice.SingleDevice(mockClient, Config.deviceConf(0));

        executor.execute(device);

        executor.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);

        executor.shutdown();

        // mock asserts
        verify(mockClient, times(1)).connect();

        for (SampleConfig sampConf : Config.getSampleConfs(0)) {
            verify(mockClient, times(2)).publish(eq(sampConf.getTopic()), anyString());
        }
    }

    @Test
    public void runThreeDevicesTest() throws InterruptedException {

        ItemConfig itemA = new ItemNumConfig("widget", ItemType.Double, 0, 100, 1);
        ItemConfig itemB = new ItemStringConfig("bird", ItemType.String,
                Arrays.asList("Albatross","Jay","Magpie"));

        SampleConfig sampleConfA = new SampleConfig("SampleA","SampleA","test/foo",
                Arrays.asList(itemA, itemB));
        SampleConfig sampleConfB = new SampleConfig("SampleB","SampleB","test/bar",
                Arrays.asList(itemA, itemB));
        SampleConfig sampleConfC = new SampleConfig("SampleC","SampleC","test/kilroy",
                Arrays.asList(itemA, itemB));

        List<DeviceConfig> testDevices = Arrays.asList(
                new DeviceConfig("random","Test Device A", "First Device for Testing",
                        Arrays.asList(sampleConfA), 1000l, 0l, 1),
                new DeviceConfig("random","Test Device B", "Second Device for Testing",
                        Arrays.asList(sampleConfB), 1000l, 0l, 1),
                new DeviceConfig("random","Test Device C", "Third Device for Testing",
                        Arrays.asList(sampleConfC), 1000l, 0l, 1)
                );

        Config.getRunnerConfig().setDevices(testDevices);
        Config.getRunnerConfig().setTtl(3000l);

        ExecutorService executor = Executors.newFixedThreadPool(Config.getDeviceConfs().size());

        for(DeviceConfig devConf : Config.getDeviceConfs()){
            GenericDevice genDev = GenericDevice.SingleDevice(mockClient, devConf);
            executor.execute(genDev);
        }

        executor.awaitTermination(Config.ttl() * 2, TimeUnit.MILLISECONDS);

        executor.shutdown();

        verify(mockClient, times(3)).connect();
        for(DeviceConfig devConf : Config.getDeviceConfs()){
            verify(mockClient, times(3)).publish(
                    eq(devConf.getSample(0).getTopic()), anyString());
        }
    }

    @Test
    public void threeDevicesOwnClientTwoSamplesTest() throws InterruptedException {

        MqttClientBlocking mockClientA = mock(MqttClientBlocking.class);
        MqttClientBlocking mockClientB = mock(MqttClientBlocking.class);
        MqttClientBlocking mockClientC = mock(MqttClientBlocking.class);

        ItemConfig itemAA = new ItemNumConfig("tribble", ItemType.Double, 0, 100, 1);
        ItemConfig itemAB = new ItemStringConfig("cat", ItemType.String,
                Arrays.asList("Manx","Siamese","Calico"));
        ItemConfig itemBA = new ItemNumConfig("whatsit", ItemType.Double, -1, 1, 2);
        ItemConfig itemBB = new ItemStringConfig("dog", ItemType.String,
                Arrays.asList("Labrador","Collie","Beagle"));

        SampleConfig sampleConfA = new SampleConfig("SampleA","SampleA","test/tricoder",
                Arrays.asList(itemAA, itemAB));
        SampleConfig sampleConfB = new SampleConfig("SampleB","SampleB","test/raygun",
                Arrays.asList(itemBA, itemBB));

        List<Device> testDevices = Arrays.asList(
                GenericDevice.SingleDevice(mockClientA, new DeviceConfig("random","Test Device A", "First Device for Testing",
                        Arrays.asList(sampleConfA, sampleConfB), 1000l, 0l, 1)),
                GenericDevice.SingleDevice(mockClientB, new DeviceConfig("random","Test Device B", "Second Device for Testing",
                        Arrays.asList(sampleConfA, sampleConfB), 1000l, 0l, 1)),
                GenericDevice.SingleDevice(mockClientC, new DeviceConfig("random","Test Device C", "Third Device for Testing",
                        Arrays.asList(sampleConfA, sampleConfB), 1000l, 0l, 1))
                );

        Config.getRunnerConfig().setDevices(testDevices.stream()
                .map(device -> device.getConfig())
                .collect(Collectors.toList()));

        Config.getRunnerConfig().setTtl(3000l);

        ExecutorService executor = Executors.newFixedThreadPool(testDevices.size());

        for(Device device : testDevices){
            executor.execute(device);
        }

        executor.awaitTermination(Config.ttl() + 100, TimeUnit.MILLISECONDS);

        executor.shutdown();

        // asserts
        for(Device device: testDevices){
            // Connection called only once
            verify(((GenericDevice)device).getClient(),times(1)).connect();
            // First Sample published three times
            verify(((GenericDevice)device).getClient(),times(3)).publish(
                    eq(device.getConfig().getSample(0).getTopic()), anyString()
            );
            // Second Sample published three times
            verify(((GenericDevice)device).getClient(),times(3)).publish(
                    eq(device.getConfig().getSample(1).getTopic()), anyString()
            );
        }
    }

    @Test
    public void jitterTest() throws InterruptedException {
        ItemConfig iConf = new ItemNumConfig("testItem", ItemType.Double, 0, 100, 1);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Config.getRunnerConfig().sampleConf(0, 0).setItems(Arrays.asList(iConf));
        Config.getRunnerConfig().setTtl(3000l);

        assertEquals(2, Config.getDeviceConfs().size());
        assertEquals(2, Config.getSampleConfs(0).size());

        DeviceConfig devConf = Config.deviceConf(0);

        SampleConfig sampConf = Config.sampleConf(0, 0);

        devConf.setInterval(1000l);
        devConf.setJitter(500l);

        GenericDevice device = GenericDevice.SingleDevice(mockClient, devConf);

        executor.execute(device);

        executor.awaitTermination(Config.ttl() + 500, TimeUnit.MILLISECONDS);

        executor.shutdown();

        verify(mockClient, times(1)).connect();
        verify(mockClient, times(2)).publish(eq(sampConf.getTopic()), anyString());
    }

    @Test
    public void deviceCopyTest(){
        ItemConfig itemConfA = new ItemNumConfig("volt", ItemType.Double, 1, 15, 2);
        ItemConfig itemConfB = new ItemNumConfig("pulses", ItemType.Long, 0l, 20l, 1);
        ItemConfig itemConfC = new ItemStringConfig("state", ItemType.String, Arrays.asList("OK", "WARN", "CRIT"));

        SampleConfig sampConfA = new SampleConfig("random", "testSample", "test/copy",
                Arrays.asList(itemConfA, itemConfB, itemConfC));
        SampleConfig sampConfB = new SampleConfig ("beta-sample", "betaSample", "test/beta",
                Arrays.asList(itemConfA));

        DeviceConfig devConf = new DeviceConfig("random", "testDevice", "device for testing",
                Arrays.asList(sampConfA, sampConfB), 3000l, 500l, 2);

        DeviceConfig copyConf = new DeviceConfig(devConf, 2);

        // ensure deep copy
        assertNotEquals(devConf.hashCode(), copyConf.hashCode());
        assertEquals( devConf.getInterval(), copyConf.getInterval());
        assertEquals(devConf.getJitter(), copyConf.getJitter());
        assertEquals(devConf.getDescription(), copyConf.getDescription());
        assertEquals(1, copyConf.getCount());
        assertEquals(String.format("%s-%03d", devConf.getId(), 2), copyConf.getId());
        assertEquals(String.format("%s-%03d", devConf.getName(), 2), copyConf.getName());

        for(ListIterator<SampleConfig> itConf = devConf.getSamples().listIterator();
            itConf.hasNext(); ){
            SampleConfig devSampleConf = itConf.next();
            // ensure deep copy of samples
            assertNotEquals(devSampleConf.hashCode(), copyConf.getSamples().get(itConf.previousIndex()).hashCode());
            assertEquals(String.format("%s-%03d", devSampleConf.getId(), 2),
                    copyConf.getSamples().get(itConf.previousIndex()).getId());
            assertEquals(String.format("%s-%03d", devSampleConf.getName(), 2),
                    copyConf.getSamples().get(itConf.previousIndex()).getName());
        }

    }

    // TODO no id with value "RANDOM" test.
}

