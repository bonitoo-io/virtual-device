package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hivemq.client.internal.mqtt.MqttRxClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import io.bonitoo.qa.DeviceRunner;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.Mode;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.Sample;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.mqtt.client.MqttClientRx;
import io.bonitoo.qa.mqtt.client.VirDevMqttClientBuilder;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subscribers.TestSubscriber;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("intg")
@ExtendWith(MockitoExtension.class)
public class ReactiveDeviceTest {

  static long origTTL;

  @Mock
  MqttRxClient mHiveClient;

  @Mock
  Mqtt5ConnAck mockHiveAck;

  @BeforeEach
  public void setup() throws InterruptedException {
    reset(mHiveClient);
    reset(mockHiveAck);
    lenient().when(mHiveClient.connect()).thenReturn(Single.just(mockHiveAck));
    lenient().when(mHiveClient.publish(any())).thenReturn(Flowable.just(new Mqtt5PublishResult() {
      @Override
      public @NotNull Mqtt5Publish getPublish() {
        return null;
      }

      @Override
      public @NotNull Optional<Throwable> getError() {
        return Optional.empty();
      }
    }));
    lenient().when(mHiveClient.disconnect()).thenReturn(Completable.fromAction(() -> {}));
    Config.reset();
    // N.B. changing values in static Config can impact other tests
    origTTL = Config.ttl();
  }

  @AfterEach
  public void resetAny(){
    // N.B. changing values in static Config can impact other tests
    Config.getRunnerConfig().setTtl(origTTL);
  }

  @Test
  public void genericDeviceRxBaseTest() throws InterruptedException {

    ItemConfig iConf = new ItemNumConfig("testItem", "anyVal", ItemType.Double, 0, 100, 1.0, NumGenerator.DEFAULT_DEV);

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Config.getRunnerConfig().sampleConf(0, 0).setItems(Arrays.asList(iConf));
    Config.getRunnerConfig().setTtl(1000l);
    Config.getRunnerConfig().setMode(Mode.REACTIVE);
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(Config.getBrokerConf());
    builder.id("ASDF Client");
    MqttClientRx clientRx = builder.buildRx();
    clientRx.setClient(mHiveClient);

    assertEquals(2, Config.getDeviceConfs().size());
    assertEquals(2, Config.getSampleConfs(0).size());

    DeviceConfig devConf = Config.deviceConf(0);
    devConf.setInterval(500L);

    GenericDevice device = GenericDevice.singleDevice(clientRx, devConf);

    executor.execute(device);

    executor.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);

    executor.shutdown();

    verify(mHiveClient, times(1)).connect();
    // N.B. publish **Flowable** in Reactivex gets called only once - then flow begins
    verify(mHiveClient, times(1)).publish(any());
    verify(mHiveClient, times(1)).disconnect();
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

  // N.B. using reactivex TestSubscriber
  @Test
  public void checkFlowableSamples() throws InterruptedException, JsonProcessingException {

    ItemConfig iConf1 = new ItemNumConfig("adam", "zebra", ItemType.Double, 0, 100, 1.0, NumGenerator.DEFAULT_DEV);
    ItemConfig iConf2 = new ItemNumConfig("baker", "yuma", ItemType.Double, -25, 25, 1.0, NumGenerator.DEFAULT_DEV);
    ItemConfig iConf3 = new ItemNumConfig("charlie", "x-ray", ItemType.Long, 1, 55, 1.0, NumGenerator.DEFAULT_DEV);

    SampleConfig sConf1 = new SampleConfig("deltaTest01", "delta", "test/delta", Collections.singletonList(iConf1));
    SampleConfig sConf2 = new SampleConfig("easyTest01", "easy", "test/easy", Arrays.asList(iConf2, iConf3));

    // ClientRx Needed to create device, but will be ignored
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(Config.getBrokerConf());
    builder.id("ASDF Client");
    MqttClientRx clientRx = builder.buildRx();
    clientRx.setClient(mHiveClient);

    DeviceConfig devConf = new DeviceConfig("foxDevice01","foxDevice", "A test Device", Arrays.asList(sConf1, sConf2), 500L, 0L, 1);

    GenericDevice device = new GenericDevice(clientRx, devConf, 1);

    Flowable<Sample> sampleFlowable = Flowable.fromIterable(device.getSampleList())
      .doOnNext(Sample::update)
      .doOnError(System.err::println)
      .doOnComplete(() -> {
//        System.out.println("Flowable<Sample> complete on thread " + Thread.currentThread().getName());
      })
      .doOnCancel(() -> {
        System.out.println("Flowable<Sample> cancelled");
      })
      .repeat(5);

    TestSubscriber<Sample> testSubscriber = sampleFlowable.test();

    testSubscriber.await(1000L, TimeUnit.MILLISECONDS);

    testSubscriber.assertComplete();
    testSubscriber.assertNoErrors();
    testSubscriber.assertValueCount(10);

    IntStream.range(0, testSubscriber.values().size()).forEach(idx ->
      {
          Sample s = testSubscriber.values().get(idx);
          if(idx % 2 == 0){
            assertEquals("deltaTest01", s.getId());
            assertEquals(1, s.getItems().size());
            assertEquals("zebra", s.getItems().get("adam").get(0).getLabel());
            Double dAdam = s.getItems().get("adam").get(0).asDouble();
            assertTrue(dAdam >= 0 && dAdam <= 200);
          }else{
            assertEquals("easyTest01", s.getId());
            assertEquals(2, s.getItems().size());
            assertEquals("yuma", s.getItems().get("baker").get(0).getLabel());
            assertEquals("x-ray", s.getItems().get("charlie").get(0).getLabel());
            Double dBaker = s.getItems().get("baker").get(0).asDouble();
            assertTrue(dBaker >= -50 && dBaker <= 50);
            Long lCharlie = s.getItems().get("charlie").get(0).asLong();
            assertTrue(lCharlie >= 0L && lCharlie <= 110L);
          }
      }
    );

  }

}
