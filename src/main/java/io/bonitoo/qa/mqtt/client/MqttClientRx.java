package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  Mqtt5RxClient client;

  protected MqttClientRx() {
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
    Mqtt5ClientBuilder clientBuilder = Mqtt5Client.builder()
      .identifier(id)
      .serverHost(broker.getHost())
      .serverPort(broker.getPort());

    if(broker.getAuth() != null){
      clientBuilder.simpleAuth(Mqtt5SimpleAuth.builder()
        .username(broker.getAuth().getUsername())
        .password(new String(broker.getAuth().getPassword()).getBytes())
        .build());
    }
    // TODO TLS
    mcr.client = clientBuilder.buildRx();
    return mcr;
  }

  @Override
  public MqttClient connect() throws InterruptedException {

    if (!client.connect()
        .doOnSuccess(System.out::println)
        .doOnError(System.err::println)
        .ignoreElement()
        .blockingAwait(5000, TimeUnit.MILLISECONDS)) {
      throw new VirtualDeviceRuntimeException("Failed to connect to MqttBroker at "
          + client.getConfig().getServerHost() + ":"
          + client.getConfig().getServerPort());
    }
    return this;
  }

  @Override
  public MqttClient connectSimple(String username, String password) throws InterruptedException {
    // N.B. credentials are already handled in constructor
    return this.connect();
  }

  @Override
  public MqttClient connectAnon() throws InterruptedException {

    return this.connect();

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
        .doOnComplete(() -> logger.info("Rx Disconnected"))
        .doOnError(throwable -> logger.error("Failed to disconnect " + throwable));

    completable.blockingAwait(5000, TimeUnit.MILLISECONDS);

    return this;
  }

  @Override
  public void shutdown() {

  }
}
