package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.util.KeyStoreUtil;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.Mode;
import io.bonitoo.qa.conf.mqtt.broker.AuthConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.conf.mqtt.broker.TlsConfig;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder for generating the various possible MqttClients.
 */
public class VirDevMqttClientBuilder {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  AuthConfig authConfig;
  TlsConfig tlsConfig;

  BrokerConfig brokerConfig;

  String id;

  /**
   * Constructs a new builder beginning with the brokerConfig.
   *
   * @param brokerConfig - a base BrokerConfig.
   */
  public VirDevMqttClientBuilder(BrokerConfig brokerConfig) {
    this.brokerConfig = brokerConfig;
    this.authConfig = brokerConfig.getAuth();
    this.tlsConfig = brokerConfig.getTls();
    id = UUID.randomUUID().toString();
  }

  private Mqtt5ClientBuilder startClientBuilder() {

    Mqtt5ClientBuilder clientBuilder = Mqtt5Client.builder()
        .identifier(this.id)
        .serverHost(this.brokerConfig.getHost())
        .serverPort(this.brokerConfig.getPort());

    if (this.authConfig != null) {
      clientBuilder.simpleAuth(Mqtt5SimpleAuth.builder()
          .username(this.authConfig.getUsername())
          .password(new String(this.authConfig.getPassword()).getBytes())
          .build());
    }

    if (this.tlsConfig != null) {
      try {
        TrustManagerFactory trustManagerFactory = KeyStoreUtil
            .trustManagerFromKeystore(new File(this.tlsConfig.getTrustStore()),
              new String(this.tlsConfig.getTrustPass()));
        clientBuilder.sslConfig(MqttClientSslConfig.builder()
            .keyManagerFactory(null)
            .trustManagerFactory(trustManagerFactory)
            .build()
        );
      } catch (SSLException e) {
        throw new RuntimeException(e);
      }
    }

    return clientBuilder;
  }

  /**
   * Builds a new blocking MqttClient.
   *
   * @return - a new MqttClientBlocking instance.
   */
  public MqttClientBlocking buildBlocking() {
    MqttClientBlocking clientBlocking = new MqttClientBlocking();
    clientBlocking.setBroker(this.brokerConfig);
    clientBlocking.setId(this.id);
    clientBlocking.setClient(startClientBuilder().buildBlocking());
    return clientBlocking;
  }

  /**
   * Builds a reactivex based MqttClient.
   *
   * @return - a new MqttClientRx instance.
   */
  public MqttClientRx buildRx() {
    MqttClientRx clientRx = new MqttClientRx();
    clientRx.setBroker(this.brokerConfig);
    clientRx.setId(this.id);
    clientRx.setClient(startClientBuilder().buildRx());
    return clientRx;
  }

  /**
   * Builds an AsyncClient based on previously defined values.
   *
   * @return - a new MqttClientAssync instance.
   */
  public MqttClientAsync buildAsync() {
    MqttClientAsync clientAsync = new MqttClientAsync();
    clientAsync.setBroker(this.brokerConfig);
    clientAsync.setId(this.id);
    clientAsync.setClient(startClientBuilder().buildAsync());
    return clientAsync;
  }

  /**
   * Adds an AuthConfig to the builder.
   *
   * @param authconfig - AuthConfig to add.
   * @return - this builder.
   */
  public VirDevMqttClientBuilder authConfig(AuthConfig authconfig) {
    this.authConfig = authconfig;
    return this;
  }

  /**
   * Adds a TlsConfig to the builder.
   *
   * @param tlsconfig - TlsConfig to be added.
   * @return - this builder.
   */
  public VirDevMqttClientBuilder tlsConfig(TlsConfig tlsconfig) {
    this.tlsConfig = tlsconfig;
    return this;
  }

  /**
   * Adds a BrokerConfig to the builder.
   *
   * @param brokerconfig - BrokerConfig to be added.
   * @return - this builder.
   */
  public VirDevMqttClientBuilder brokerConfig(BrokerConfig brokerconfig) {
    this.brokerConfig = brokerconfig;
    this.tlsConfig = brokerconfig.getTls();
    this.authConfig = brokerconfig.getAuth();
    return this;
  }

  public VirDevMqttClientBuilder id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Factory style method for creating different types of clients.
   *
   * @param mode - the mode of client to be generated.
   * @return - a new MqttClient.
   */
  public MqttClient genClientFromMode(Mode mode) {
    switch (mode) {
      case ASYNC:
        logger.info("Generating Async Client");
        // return MqttClientAsync.client(config, id);
        return buildAsync();
      case REACTIVE:
        logger.info("Generating Reactive Client");
        // return MqttClientRx.client(config, id);
        return buildRx();
      case BLOCKING:
        logger.info("Generating Blocking Client");
        // return MqttClientBlocking.client(config, id);
        return buildBlocking();
      default:
        throw new VirtualDeviceRuntimeException(
          "Cannot create client of unknown mode: " + mode
        );
    }

  }

}
