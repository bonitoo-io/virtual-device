package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.mqtt.broker.AuthConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrokerConfigTest {

    public static String authConfigYaml = "---\n" +
            "username: musashi\n" +
            "password: changeit";

    public static String brokerConfigYAML = "---\n" +
            "host: localhost\n" +
            "port: 1883\n" +
            "auth:\n" +
            "  username: fred\n" +
            "  password: changeit";

    @Test
    public void parseAuthConfig() throws JsonProcessingException{
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        AuthConfig conf = om.readValue(authConfigYaml, AuthConfig.class);

        assertEquals("musashi", conf.getUsername());
        assertEquals("changeit", conf.getPassword());

    }
    @Test
    public void parseBrokerConfig() throws JsonProcessingException {

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        BrokerConfig conf = om.readValue(brokerConfigYAML, BrokerConfig.class);

        assertEquals("localhost", conf.getHost());
        assertEquals(1883, conf.getPort());
        assertEquals(new AuthConfig("fred", "changeit"), conf.getAuth());

    }
}
