package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.Mode;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.data.Sample;
import io.bonitoo.qa.mqtt.client.MqttClient;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.mqtt.client.MqttClientRx;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.sample.SamplePluginConfig;
import io.bonitoo.qa.plugin.sample.SamplePluginMill;
import io.bonitoo.qa.util.LogHelper;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import io.bonitoo.qa.util.VirDevWorkInProgressException;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * A Generic device configurable with a DeviceConfig.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenericDevice extends Device {

  int number;

  MqttClient client;

  protected GenericDevice(MqttClient client, DeviceConfig config, int number) {
    this.config = config;
    this.sampleList = new ArrayList<>();
    this.client = client;
    this.number = number;
    for (SampleConfig sc : config.getSamples()) {
      if (sc instanceof SamplePluginConfig) { // add a plugin
        try {
          this.sampleList.add(SamplePluginMill.genNewInstance((SamplePluginConfig) sc));
        } catch (PluginConfigException | InvocationTargetException | NoSuchMethodException
                 | InstantiationException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      } else {
        this.sampleList.add(GenericSample.of(sc));
      }
    }
  }

  public static GenericDevice singleDevice(MqttClient client, DeviceConfig config) {
    return numberedDevice(client, config, 1);
  }

  /**
   * Generates a device which is one of a series using the same base config.  The new device
   * will be tagged with an additional serial number.
   *
   * @param client - the client that the device will use to communicate with an MQTT broker.
   * @param config - configuration for the device.
   * @param number - serial number for the device to be added to id and name fields in samples.
   * @return - a generic device.
   */
  public static GenericDevice numberedDevice(MqttClient client,
                                             DeviceConfig config, int number) {
    return new GenericDevice(client, config, number);
  }


  public void blockingRun(long ttl) throws InterruptedException {

    if (! (this.client instanceof MqttClientBlocking)) {
      throw new VirtualDeviceRuntimeException(
        "Attempt to start blockingRun with non-blocking client " + this.client.getClass().getName()
      );
    }

    try {
      if (config.getJitter() > 0) {
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getJitter() * number));
      }
      logger.info(LogHelper.buildMsg(config.getId(), "Device Connection", ""));
      client.connect();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    try {
      while (System.currentTimeMillis() < ttl) {
        logger.debug(LogHelper.buildMsg(config.getId(),
            "Wait to publish",
            Long.toString((ttl - System.currentTimeMillis()))));
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getJitter()));
        for (Sample sample : sampleList) {
          String jsonSample = sample.update().toJson();
          logger.info(LogHelper.buildMsg(sample.getId(), "Publishing", jsonSample));
          client.publish(sample.getTopic(), jsonSample);
        }
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getInterval()));
      }
      logger.debug(LogHelper.buildMsg(config.getId(),
          "Published",
          Long.toString((ttl - System.currentTimeMillis()))));
    } catch (JsonProcessingException | InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      client.disconnect();
    }
  }

  public void reactiveRun(long ttl) {

    if (! (this.client instanceof MqttClientRx)) {
      throw new VirtualDeviceRuntimeException(
        "Attempt to start reactiveRun with non-reactive client " + this.client.getClass().getName()
      );
    }

    try {
      if (config.getJitter() > 0) {
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getJitter() * number));
      }
      logger.info(LogHelper.buildMsg(config.getId(), "Device Connection", ""));
      client.connect();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    // todo set delay based on config
    Flowable<Mqtt5Publish> messageFlow = Flowable.fromIterable(sampleList)
        .concatMap(sample -> {
//        sample.update();
          Flowable<Sample> fs = Flowable.just(sample).delay(1000, TimeUnit.MILLISECONDS, Schedulers.io());
//        System.out.println("DEBUG sample on thread " + Thread.currentThread().getName() + ":\n" + sample.toJson());
          return fs;
        })
        .doOnNext(sample -> {
          sample.update();
          logger.debug("sample on thread " + Thread.currentThread().getName() + ":\n" + sample.toJson());
        })
        .doOnError(System.err::println)
        .doOnComplete(() -> {
          logger.debug("Flowable<Sample> complete on thread " + Thread.currentThread().getName());
        })
        .doOnCancel(() -> {
          logger.error("Flowable<Sample> cancelled");
        })
        .map(sample ->
          Mqtt5Publish.builder()
            .topic(sample.getTopic())
            .qos(MqttQos.EXACTLY_ONCE)
            .payload(sample.toJson().getBytes())
            .build()
        )
        .repeatUntil(new BooleanSupplier() {
          @Override
          public boolean getAsBoolean() throws Exception {
            return Instant.now().toEpochMilli() > ttl;
          }
        });

    Disposable disposable = ((MqttClientRx) client).getClient().publish(messageFlow)
        .doOnNext(pubRes -> logger.debug(pubRes.toString()))
        .doOnError(System.err::println)
        .doOnComplete(() -> logger.debug(Thread.currentThread().getName() + " publishing samples complete"))
        .subscribe();

    while (!disposable.isDisposed()) {
      System.out.println("Waiting for samples...");
      LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(3000));
    }

    try {
      client.disconnect();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  public void asyncRun(long ttl) {
    throw new VirDevWorkInProgressException("asyncRun logic TBD");
  }

  @Override
  public void run() {

    long ttl = System.currentTimeMillis() + Config.ttl();

    if (Config.getRunnerConfig().getMode() == Mode.REACTIVE ) {
      reactiveRun(ttl);
    } else if (Config.getRunnerConfig().getMode() == Mode.ASYNC) {
      asyncRun(ttl);
    } else {
      try {
        blockingRun(ttl);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    /* try {
      if (config.getJitter() > 0) {
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getJitter() * number));
      }
      logger.info(LogHelper.buildMsg(config.getId(), "Device Connection", ""));
      client.connect();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    try {
      while (System.currentTimeMillis() < ttl) {
        logger.debug(LogHelper.buildMsg(config.getId(),
            "Wait to publish",
            Long.toString((ttl - System.currentTimeMillis()))));
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getJitter()));
        for (Sample sample : sampleList) {
          String jsonSample = sample.update().toJson();
          logger.info(LogHelper.buildMsg(sample.getId(), "Publishing", jsonSample));
          client.publish(sample.getTopic(), jsonSample);
        }
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getInterval()));
      }
      logger.debug(LogHelper.buildMsg(config.getId(),
          "Published",
          Long.toString((ttl - System.currentTimeMillis()))));
    } catch (JsonProcessingException | InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      client.disconnect();
    } */

  }
}
