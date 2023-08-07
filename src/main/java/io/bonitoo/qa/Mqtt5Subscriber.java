package io.bonitoo.qa;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.util.KeyStoreUtil;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple subscriber class used to inspect the broker.
 */
public class Mqtt5Subscriber {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  static final String defaultTopic = "test/#";
  static final String DEFAULT_HOST = "127.0.0.1";
  static final String DEFAULT_TRUSTSTORE = "./scripts/keys/brokerTrust.jks";
  static final String TRUSTSTORE_PASSWORD = "changeit";
  static final Integer DEFAULT_TLS_PORT = 8883;
  static final Integer DEFAULT_PORT = 1883;


  /**
   *  Starting point.
   *
   * @param args - standard CLI args.
   */
  public static void main(String[] args) throws IOException {

    boolean useTls = Boolean.parseBoolean(System.getProperty("sub.tls", "false"));
    String hostname = System.getProperty("broker.host", DEFAULT_HOST);
    Integer hostport = Integer.valueOf(System.getProperty("broker.port",
        useTls ? String.valueOf(DEFAULT_TLS_PORT) : String.valueOf(DEFAULT_PORT)));

    String topic = System.getProperty("sub.topic", defaultTopic);

    for (String arg : args) {
      System.out.println(arg);
      if (arg.matches("^topic=.*")) {
        topic = arg.split("=")[1];
      }
    }

    Mqtt5BlockingClient client;

    if (useTls) {
      client = createTls(hostname, hostport);
    } else {
      client = createPlain(hostname, hostport);
    }

    client.connectWith().cleanStart(false).send();

    CompletableFuture<Mqtt5SubAck> futureSubAck = client.toAsync().subscribeWith()
        .topicFilter(topic)
        .qos(MqttQos.AT_LEAST_ONCE)
        .callback(publish -> {
          String content = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();
          logger.info(String.format(
              "Received on topic \"%s\", with payload %dbyte(s) and qos %s:\n%s",
              publish.getTopic(), publish.getPayloadAsBytes().length, publish.getQos(), content));
        })
        .send();

    logger.info(String.format("Subscribed to topic \"%s\"", topic));

  }

  /**
   * Creates a plain - unexcrypted HTTP connection.
   *
   * @param hostName - target broker hostname.
   * @param hostPort - target broker port.
   * @return - a client that can connect to the target broker.
   */
  public static Mqtt5BlockingClient createPlain(String hostName, Integer hostPort) {
    logger.info(String.format("Creating client to %s:%d unsecure.", hostName, hostPort));

    return Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .identifier("testClient")
        .serverHost(hostName)
        .serverPort(hostPort)
        .buildBlocking();
  }

  /**
   * Creates a TLS encrypted connection.
   *
   * @param hostName - hostname of the target broker.
   * @param hostPort - target broker port.
   * @return - a client that can connect to the broker.
   * @throws IOException -
   */
  public static Mqtt5BlockingClient createTls(String hostName, Integer hostPort)
      throws IOException {

    // TODO add use of ENV VARS for Truststore and Password

    final TrustManagerFactory trustManagerFactory = KeyStoreUtil
        .trustManagerFromKeystore(new File(DEFAULT_TRUSTSTORE),
        TRUSTSTORE_PASSWORD);

    logger.info(String.format("Creating client to %s:%d with TLS.", hostName, hostPort));
    logger.info(String.format("Using trust store %s.", DEFAULT_TRUSTSTORE));

    return MqttClient
        .builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost(hostName)
        .serverPort(hostPort)
        .sslConfig(MqttClientSslConfig.builder()
          .keyManagerFactory(null)
          .trustManagerFactory(trustManagerFactory)
          .build()
        )
        .useMqttVersion5()
        .buildBlocking();
  }
}
