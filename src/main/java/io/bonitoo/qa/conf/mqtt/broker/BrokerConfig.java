package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A base configuration for an MQTT5 broker.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = BrokerConfigDeserializer.class)
public class BrokerConfig {

  String host;
  int port;

  AuthConfig auth;

  // optional
  @JsonInclude(JsonInclude.Include.NON_NULL)
  TLSConfig tls;

  public BrokerConfig(String host, int port, AuthConfig auth) {
    this.host = host;
    this.port = port;
    this.auth = auth;
    this.tls = null;
  }

  @Override
  public String toString() {
    return String.format("host=%s,port=%d,auth={%s},tls={%s}", host, port, auth, tls);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof BrokerConfig)) {
      return false;
    }

    final BrokerConfig conf = (BrokerConfig) obj;

    return conf.getHost().equals(host)
      && conf.getPort() == port
      && conf.getAuth().equals(auth);
  }
}
