package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import io.bonitoo.qa.conf.mqtt.broker.AuthConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.conf.mqtt.broker.TlsConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/*
N.B. client.connect and client.publish can be uncommented to manually verify
that the built clients actually work.
 */
@Tag("unit")
public class VirDevMqttClientBuilderTest {

  @Test
  public void buildBlockingPlain() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 1883, null);
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    MqttClientBlocking client = builder.buildBlocking();

//    client.connect();

    assertInstanceOf(Mqtt5BlockingClient.class, client.getClient());
    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertFalse(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());

  }

  @Test
  public void buildBlockingSimpleAuth01() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 1883, null);
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.authConfig(new AuthConfig("tester", "changeit".toCharArray()));
    MqttClientBlocking client = builder.buildBlocking();

   // client.connect();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());
  }

  @Test
  public void buildBlockingSimpleAuth02() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 1883,
      new AuthConfig("tester", "changeit".toCharArray()));
    MqttClientBlocking client = (new VirDevMqttClientBuilder(config)).buildBlocking();

//    client.connect();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertNotNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());

  }

  @Test
  public void buildBlockingTLS() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 8883, null);

    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.tlsConfig(new TlsConfig("./scripts/keys/brokerTrust.jks","changeit".toCharArray()));
    builder.id("TestClient");

    MqttClientBlocking client = builder.buildBlocking();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(8883, client.getClient().getConfig().getServerPort());
    assertNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSslConfig().isPresent());

//    client.connect();

//    client.publish("test/pokus", "TEST FROM UNIT TEST BLOCKING");

  }

  @Test
  public void buildBlockingAllInConfig() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 8883,
      new AuthConfig("tester", "changeit".toCharArray()),
      new TlsConfig("./scripts/keys/brokerTrust.jks","changeit".toCharArray()));

    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.id("TestClient");

    MqttClientBlocking client = builder.buildBlocking();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(8883, client.getClient().getConfig().getServerPort());
    assertNotNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSslConfig().isPresent());

//    client.connect();

//    client.publish("test/pokus", "TEST FROM UNIT TEST BLOCKING");

  }

  @Test
  public void buildRXSimple() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 1883, null);
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    MqttClientRx client = builder.buildRx();

//    client.connect();

    assertInstanceOf(Mqtt5RxClient.class, client.getClient());
    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertFalse(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());

  }

  @Test
  public void buildRXSimpleAuth01() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 1883, null);
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.authConfig(new AuthConfig("tester", "changeit".toCharArray()));
    MqttClientRx client = builder.buildRx();

//    client.connect();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());

  }

  @Test
  public void buildRXSimpleAuth02() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 1883,
      new AuthConfig("tester", "changeit".toCharArray()));
    MqttClientRx client = (new VirDevMqttClientBuilder(config)).buildRx();

//    client.connect();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertNotNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());

  }

  @Test
  public void buildRxTls() throws InterruptedException {
    BrokerConfig config = new BrokerConfig("localhost", 8883, null);

    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.tlsConfig(new TlsConfig("./scripts/keys/brokerTrust.jks","changeit".toCharArray()));
    builder.id("TestClient");

    MqttClientRx client = builder.buildRx();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(8883, client.getClient().getConfig().getServerPort());
    assertNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSslConfig().isPresent());

//    client.connect();

//    client.publish("test/pokus", "TEST FROM UNIT TEST RX");

  }

  @Test
  public void buildRxTlsSimpleAuth() throws InterruptedException {
    BrokerConfig config = new BrokerConfig("localhost", 8883, null);

    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.tlsConfig(new TlsConfig("./scripts/keys/brokerTrust.jks","changeit".toCharArray()));
    builder.id("TestClient");
    builder.authConfig(new AuthConfig("tester","changeit".toCharArray()));

    MqttClientRx client = builder.buildRx();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(8883, client.getClient().getConfig().getServerPort());
    assertNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSslConfig().isPresent());

//    client.connect();

//    client.publish("test/pokus", "TEST FROM UNIT TEST RX");

  }

  @Test
  public void buildRXAllInConfig() throws InterruptedException {
    BrokerConfig config = new BrokerConfig("localhost", 8883,
      new AuthConfig("tester", "changeit".toCharArray()),
      new TlsConfig("./scripts/keys/brokerTrust.jks","changeit".toCharArray()));

    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.id("TestClient");

    MqttClientRx client = builder.buildRx();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(8883, client.getClient().getConfig().getServerPort());
    assertNotNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSslConfig().isPresent());

//    client.connect();

//    client.publish("test/pokus", "TEST FROM UNIT TEST RX");

  }

  @Test
  public void buildAsyncSimple() throws InterruptedException {
    BrokerConfig config = new BrokerConfig("localhost", 1883, null);
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    MqttClientAsync client = builder.buildAsync();

//    client.connect();

    assertInstanceOf(Mqtt5AsyncClient.class, client.getClient());
    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertFalse(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());

//    client.publish("test/essaie", "TEST FROM UNIT TEST ASYNC");
  }

  @Test
  public void buildAsyncSimpleAuth01() throws InterruptedException {

    BrokerConfig config = new BrokerConfig("localhost", 1883, null);
    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.authConfig(new AuthConfig("tester", "changeit".toCharArray()));
    MqttClientAsync client = builder.buildAsync();

//    client.connect();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(1883, client.getClient().getConfig().getServerPort());
    assertNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSimpleAuth().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());
    assertFalse(client.getClient().getConfig().getSslConfig().isPresent());

  }

  @Test
  public void buildAsyncTls() throws InterruptedException {
    BrokerConfig config = new BrokerConfig("localhost", 8883, null);

    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.tlsConfig(new TlsConfig("./scripts/keys/brokerTrust.jks","changeit".toCharArray()));
    builder.id("TestClient");

    MqttClientAsync client = builder.buildAsync();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(8883, client.getClient().getConfig().getServerPort());
    assertNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSslConfig().isPresent());

//    client.connect();

//    client.publish("test/pokus", "TEST FROM UNIT TEST ASYNC");

  }

  @Test
  public void buildAsyncAllInConfig() throws InterruptedException {
    BrokerConfig config = new BrokerConfig("localhost", 8883,
      new AuthConfig("tester", "changeit".toCharArray()),
      new TlsConfig("./scripts/keys/brokerTrust.jks","changeit".toCharArray()));

    VirDevMqttClientBuilder builder = new VirDevMqttClientBuilder(config);
    builder.id("TestClient");

    MqttClientAsync client = builder.buildAsync();

    assertEquals("localhost", client.getClient().getConfig().getServerHost());
    assertEquals(8883, client.getClient().getConfig().getServerPort());
    assertNotNull(client.getBroker().getAuth());
    assertTrue(client.getClient().getConfig().getSslConfig().isPresent());

//    client.connect();

//    client.publish("test/pokus", "TEST FROM UNIT TEST ASYNC");

  }

}
