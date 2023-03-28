package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * A reactiveX based MqttClient.
 *
 * <p>N.B. this is initially included for experimenting with
 * MQTT brokers and the reactive idiom.
 */
@Builder
@AllArgsConstructor
@Setter
@Getter
public class MqttClientRx extends AbstractMqttClient {

  Mqtt5RxClient client;

  private MqttClientRx() {
    super();
  }

  /**
   * Generates a new Reactive based MQTT client.
   *
   * @param broker - a configuration for the broker to which the client will connect.
   * @param id - an ID for the client.
   * @return - a new MqttClientRx instance ready to connect to the broker.
   */
  public static MqttClientRx client(BrokerConfig broker, String id) {
    MqttClientRx mcr = new MqttClientRx();
    mcr.broker = broker;
    mcr.id = id;
    mcr.client = Mqtt5Client.builder()
      .identifier(id)
      .serverHost(broker.getHost())
      .serverPort(broker.getPort())
      .buildRx();
    return mcr;
  }

  @Override
  public MqttClient connect() throws InterruptedException {
    // todo implement - see MqttClientBlocking
    return null;
  }

  @Override
  public MqttClient connectSimple(String username, String password) throws InterruptedException {
    // TODO implement
    return null;
  }

  @Override
  public MqttClient connectAnon() throws InterruptedException {

    Single<Mqtt5ConnAck> singleAck = client.connect();

    Completable connectScenario = singleAck
        .doAfterSuccess(connAck -> System.out.println("Rx CONNECTED " + connAck))
        .doOnError(throwable -> System.out.println("Rx Failed to connect " + throwable))
        .ignoreElement();

    connectScenario.blockingAwait(5000, TimeUnit.MILLISECONDS);

    return this;
  }

  @Override
  public MqttClient publish(String topic, String payload) throws InterruptedException {

    Flowable<Mqtt5Publish> msg2Publish = Flowable.just(
        Mqtt5Publish.builder()
        .topic(topic)
        .qos(MqttQos.EXACTLY_ONCE)
        .payload(payload.getBytes())
        .build());

    Completable pubScenario = client.publish(msg2Publish)
        .doOnNext(publishResult -> System.out.println(
        "RX Publish Acknowledged " + new String(publishResult.getPublish().getPayloadAsBytes())
      ))
        .doOnError(throwable -> System.out.println("RX failed to publish " + throwable))
        .ignoreElements();

    pubScenario.blockingAwait(5000, TimeUnit.MILLISECONDS);

    return this;
  }

  @Override
  public MqttClient disconnect() {
    Completable completable = client.disconnect()
        .doOnComplete(() -> System.out.println("Rx Disconnected"))
        .doOnError(throwable -> System.err.println("Failed to disconnect " + throwable));

    completable.blockingAwait(5000, TimeUnit.MILLISECONDS);

    return this;
  }

  @Override
  public void shutdown() {

  }
}
