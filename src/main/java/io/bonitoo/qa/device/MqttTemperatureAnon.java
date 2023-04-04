package io.bonitoo.qa.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.data.TemperatureSample;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.data.generator.Utils;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * A device dedicated to publishing temperature data over an anonymous MQTT connection.
 *
 * <p>N.B. This class was part of the first iteration POC and should probably be
 * repurposed or removed.
 */
public class MqttTemperatureAnon extends Device {

  MqttClientBlocking client;

  private MqttTemperatureAnon() {
    super();
  }

  /**
   * Generates a temperature device with an unauthenticated MQTT blocking client.*
   *
   * @param client - an MqttClient.
   * @param config - a configuration for the device.
   * @return - the device, ready to be run.
   */
  public static MqttTemperatureAnon device(MqttClientBlocking client, DeviceConfig config) {
    MqttTemperatureAnon mqtp = new MqttTemperatureAnon();
    mqtp.client = client;
    mqtp.config = config;
    return mqtp;
  }

  @Override
  public void run() {

    long ttl = System.currentTimeMillis() + Config.ttl();

    try {
      client.connect();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    try {
      while (System.currentTimeMillis() < ttl) {
        client.publish(config.getSamples().get(0).getTopic(),
            Utils.pojoToJson(new TemperatureSample(config.getId(),
            System.currentTimeMillis(),
            NumGenerator.genTemperature(System.currentTimeMillis()))));
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getInterval()));
        //Thread.sleep(config.getInterval());
      }
    } catch (JsonProcessingException | InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      client.disconnect();
    }
  }
}
