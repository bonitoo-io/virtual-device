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
 * A dedicated device for generating temperature samples with a client
 * using simple authentication.
 *
 * <p>N.B. this as part of the initial POC iteration and should probably
 * be repurposed or removed.
 */
public class MqttTemperatureSimple extends Device {

  MqttClientBlocking client;

  private MqttTemperatureSimple() {
    super();
  }

  /**
   * Generates a basic temperature device with an MQTT blocking client.
   * It will attempt to authenticated when opening a connection, so the MQTT client
   * must include a username and password.
   *
   * <p>N.B. this was part of the initial POC iteration and should probably be
   * repurposed ar removed.
   *
   * @param client - the MQTT client used in communicating with the broker.
   * @param config - a configuration for this device type.
   * @return - a new device.
   */
  public static MqttTemperatureSimple device(MqttClientBlocking client, DeviceConfig config) {
    MqttTemperatureSimple mqts = new MqttTemperatureSimple();
    mqts.client = client;
    mqts.config = config;
    return mqts;
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
        //Thread.sleep(config.getInterval());
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.getInterval()));
      }
    } catch (JsonProcessingException | InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      client.disconnect();
    }

  }

}
