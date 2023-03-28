package io.bonitoo.qa.mqtt.client;

/**
 * Base of an MqttClient.
 */
public interface MqttClient {
  MqttClient connect() throws InterruptedException;

  MqttClient connectSimple(String username, String password) throws InterruptedException;

  MqttClient connectAnon() throws InterruptedException;

  MqttClient publish(String topic, String payload) throws InterruptedException;

  MqttClient disconnect() throws InterruptedException;

  void shutdown();
}
