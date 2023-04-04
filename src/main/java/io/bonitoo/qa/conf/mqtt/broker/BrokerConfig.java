package io.bonitoo.qa.conf.mqtt.broker;

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

  @Override
  public String toString() {
    return String.format("host=%s,port=%d,auth={%s}", host, port, auth);
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
