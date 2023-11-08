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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

public class VirDevMqttClientBuilder {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  AuthConfig authConfig;
  TlsConfig tlsConfig;

  BrokerConfig brokerConfig;

  String id;

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

  public MqttClientBlocking buildBlocking() {
    MqttClientBlocking clientBlocking = new MqttClientBlocking();
    clientBlocking.setBroker(this.brokerConfig);
    clientBlocking.setId(this.id);
    clientBlocking.setClient(startClientBuilder().buildBlocking());
    return clientBlocking;
  }

  public MqttClientRx buildRx() {
    MqttClientRx clientRx = new MqttClientRx();
    clientRx.setBroker(this.brokerConfig);
    clientRx.setId(this.id);
    clientRx.setClient(startClientBuilder().buildRx());
    return clientRx;
  }

  public MqttClientAsync buildAsync() {
    MqttClientAsync clientAsync = new MqttClientAsync();
    clientAsync.setBroker(this.brokerConfig);
    clientAsync.setId(this.id);
    clientAsync.setClient(startClientBuilder().buildAsync());
    return clientAsync;
  }

  public VirDevMqttClientBuilder authConfig(AuthConfig authconfig) {
    this.authConfig = authconfig;
    return this;
  }

  public VirDevMqttClientBuilder tlsConfig(TlsConfig tlsconfig) {
    this.tlsConfig = tlsconfig;
    return this;
  }

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
