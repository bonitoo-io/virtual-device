package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.util.KeyStoreUtil;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.util.LogHelper;
import java.io.File;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * A client that uses blocking or synchronous communications with the MQTT broker.
 *
 * <p>N.B. since virtual devices are originally intended for non-performance testing and
 * development purposes, asynchronous communication was not considered a priority,
 * so this is the basic client currently used elsewhere in the library.
 */
@Builder
@AllArgsConstructor
@Getter
@Setter
public class MqttClientBlocking extends AbstractMqttClient {

  Mqtt5BlockingClient client;

  protected MqttClientBlocking() {
    super();
  }

  /**
   * Generates a new blocking MqttClient prepared to communicate with a broker based on
   * the BrokerConfig.  The resulting client gets assigned an ID.
   *
   * @param broker - configuration of the broker with which the client will communicate.
   * @param id - an ID for the client.
   * @return - a new instance of MqttClientBlocking ready to connect to the broker.
   */
  public static MqttClientBlocking client(BrokerConfig broker, String id) {
    MqttClientBlocking mcb = new MqttClientBlocking();
    mcb.broker = broker;
    mcb.id = id;

    Mqtt5ClientBuilder clientBuilder = Mqtt5Client.builder()
        .identifier(id)
        .serverHost(broker.getHost())
        .serverPort(broker.getPort());

    if (broker.getTls() != null) {
      try {
        TrustManagerFactory trustManagerFactory = KeyStoreUtil
            .trustManagerFromKeystore(new File(broker.getTls().getTrustStore()),
              new String(broker.getTls().getTrustPass()));
        clientBuilder.sslConfig(MqttClientSslConfig.builder()
            .keyManagerFactory(null)
            .trustManagerFactory(trustManagerFactory)
            .build()
        );
      } catch (SSLException e) {
        throw new RuntimeException(e);
      }
    }

    mcb.client = clientBuilder.buildBlocking();

    return mcb;
  }

  // TODO review if this is needed
  protected MqttClientBlocking(VirDevMqttClientBuilder builder){

  }

  @Override
  public MqttClient connect() throws InterruptedException {
    if (broker.getAuth() == null || broker.getAuth().getUsername() == null) {
      return connectAnon();
    } else {
      return connectSimple(broker.getAuth().getUsername(),
        new String(broker.getAuth().getPassword()));
    }
  }

  @Override
  public MqttClientBlocking connectSimple(String username,
                                          String password)
      throws InterruptedException {
    logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
        "Connect Simple", broker.getAuth().getUsername()));

    try {
      Mqtt5ConnAck ack = client.connectWith()
          .simpleAuth()
          .username(username)
          .password(password.getBytes())
          .applySimpleAuth()
          .willPublish()
          .topic("virtual/device")
          .payload(String.format("device %s gone", id).getBytes())
          .applyWillPublish()
          .send();

      logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
          "ACK Connect",
          ack.toString()));
      logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
          "Current state",
          client.getState().toString()));

    } catch (Mqtt5MessageException e) {
      logger.error(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
          "Connect failed",
          String.format("Failed to connect as %s: %s", username, e.getMqttMessage())));
      System.exit(1);
    }

    return this;
  }

  @Override
  public MqttClientBlocking connectAnon() throws InterruptedException {
    logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
        "Connect Anonymous", ""));

    Mqtt5ConnAck ack = client.connect();

    logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
        "ACK Connect",
        ack.toString()));
    logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
        "Current state",
        client.getState().toString()));

    return this;
  }

  @Override
  public MqttClientBlocking publish(String topic, String payload) throws InterruptedException {

    logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
        "Publishing",
        String.format("[%s] - %s", topic, payload)));

    client.publishWith()
      .topic(topic)
      .payload(payload.getBytes())
        .send();

    return this;

  }

  @Override
  public MqttClientBlocking disconnect() {

    client.disconnect();

    logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
        "Disconnected", ""));

    return this;
  }

  @Override
  public void shutdown() {
    logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
        "Shutting down", ""));
  }
}
