package io.bonitoo.qa.mqtt.client;

import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractMqttClient {

    BrokerConfig broker;

}
