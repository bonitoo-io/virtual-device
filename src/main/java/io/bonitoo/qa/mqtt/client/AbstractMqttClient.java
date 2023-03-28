package io.bonitoo.qa.mqtt.client;

import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import java.lang.invoke.MethodHandles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all MQTT Clients.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractMqttClient implements MqttClient {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  String id;

  BrokerConfig broker;

}
