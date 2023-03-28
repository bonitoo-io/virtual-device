package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.util.LogHelper;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
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

  MqttClientBlocking client;

  public static GenericDevice singleDevice(MqttClientBlocking client, DeviceConfig config) {
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
  public static GenericDevice numberedDevice(MqttClientBlocking client,
                                             DeviceConfig config, int number) {
    GenericDevice device = new GenericDevice();
    device.client = client;
    device.config = config;
    device.number = number;
    return device;
  }

  @Override
  public void run() {

    long ttl = System.currentTimeMillis() + Config.ttl();

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
        for (SampleConfig sampleConf : config.getSamples()) {
          String jsonSample = GenericSample.of(sampleConf).toJson();
          logger.debug(LogHelper.buildMsg(config.getId(), "Publishing", jsonSample));
          client.publish(sampleConf.getTopic(),
              jsonSample);

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
}
