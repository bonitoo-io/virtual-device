package io.bonitoo.qa.mqtt.client;

import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractMqttClient {

    static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    String id;

    BrokerConfig broker;

}
