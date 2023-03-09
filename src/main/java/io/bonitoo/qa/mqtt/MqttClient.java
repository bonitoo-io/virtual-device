package io.bonitoo.qa.mqtt;

public interface MqttClient {
    MqttClient connect(String username, String password) throws InterruptedException;

    MqttClient connectAnon() throws InterruptedException;

    MqttClient publish(String topic, String payload) throws InterruptedException;

    MqttClient disconnect() throws InterruptedException;

    void shutdown();
}
