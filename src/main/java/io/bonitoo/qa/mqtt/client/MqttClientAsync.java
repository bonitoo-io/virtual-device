package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.internal.mqtt.message.connect.connack.MqttConnAck;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * An MQTT client leveraging Mqtt5AsyncClient for asynchronous communications
 * with the MQTT broker.
 *
 * <p>Note this is not yet leveraged with the rest of the code and is currently
 * included for experimenting.
 */
@Builder
@AllArgsConstructor
@Setter
@Getter
public class MqttClientAsync extends AbstractMqttClient {

  Mqtt5AsyncClient client;

  protected MqttClientAsync() {
    super();
  }

  /**
   * Generates an MqttClientAsync instance based on the BrokerConfig and
   * assigned an id.
   *
   * @param broker - BrokerConfig describing the broker to which the client will connect.
   * @param id - an id for the client.
   * @return - MqttClientAsync instance ready to connect to the broker.
   */
  public static MqttClientAsync client(BrokerConfig broker, String id) {
    MqttClientAsync mqc = new MqttClientAsync();
    mqc.broker = broker;
    mqc.id = id;

    mqc.client = Mqtt5Client.builder()
      .identifier(id)
      .serverHost(broker.getHost())
      .serverPort(broker.getPort())
      .buildAsync();

    return mqc;
  }

  @Override
  public MqttClient connect() throws InterruptedException {
    // todo implement - see MqttClientBlocking
    try {
      Mqtt5ConnAck ack = this.client.connect().get(5000, TimeUnit.MILLISECONDS);

      System.out.println("DEBUG ack " + ack);

    } catch (ExecutionException | TimeoutException e) {
      throw new VirtualDeviceRuntimeException("failed to connect to broker in 5 seconds", e);
    }

    return this;
  }

  @Override
  public MqttClient connectSimple(String username, String password) throws InterruptedException {
    //TODO implement
    return null;
  }

  @Override
  public MqttClientAsync connectAnon() {

    System.out.println("CONNECTING");

    CompletableFuture<Mqtt5ConnAck> cf = client.connect().whenComplete((conAck, throwable) -> {
      if (throwable != null) {
        System.err.println("Failed to connect to broker");
        System.err.println(throwable.toString());
        System.exit(1);
      }
      System.out.println("Connected to broker");
    });

    System.out.print("connecting.");
    while (!cf.isDone()) {
      System.out.print(".");
      LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(300));
    }

    return this;

  }

  @Override
  public MqttClientAsync publish(String topic, String payload) throws InterruptedException {

    System.out.println("PUBLISHING");
    if (!client.getState().isConnected()) {
      // Todo fix if connection is not anonymous
      connectAnon();
    }

    client.publishWith()
        .topic(topic)
        .payload(payload.getBytes())
        .send()
        .whenComplete((result, throwable) -> {
          if (throwable != null) {
            System.err.println("Failed to publish to topic " + topic);
            System.out.println(throwable.toString());
          } else {
            System.out.println("Publish Success " + result);
          }
        });

    return this;
  }

  @Override
  public MqttClientAsync disconnect() throws InterruptedException {

    System.out.println("DISCONNECTING");

    Mqtt5Disconnect disconnect = Mqtt5Disconnect.builder()
        .reasonCode(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION)
        .sessionExpiryInterval(600L)
        .reasonString("Planned Disconnect").build();

    if (client.getState().isConnected()) {
      client.disconnect(disconnect)
          .whenComplete((ack, throwable) -> {
            if (throwable != null) {
              System.err.println("Failed to disconnect");
              System.err.println(throwable.toString());
            } else {
              System.out.println("Disconnect Success");
            }
          });
    }

    return this;
  }

  @Override
  public void shutdown() {
  }

}
