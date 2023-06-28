package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.mqtt.broker.AuthConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
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

    public static String brokerConfigTlsYAML = "---\n" +
      "host: localhost\n" +
      "port: 8883\n" +
      "auth:\n" +
      "  username: fred\n" +
      "  password: changeit\n" +
      "tls:\n" +
      "  trustStore: \"myTrustStore.jks\"\n" +
      "  trustPass:  \"foobar\"\n";

    @Test
    public void parseAuthConfig() throws JsonProcessingException{
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        AuthConfig conf = om.readValue(authConfigYaml, AuthConfig.class);

        assertEquals("musashi", conf.getUsername());
        assertEquals("changeit", conf.getPassword());

    }
    @Test
    public void parseBrokerConfigBasic() throws JsonProcessingException {

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        BrokerConfig conf = om.readValue(brokerConfigYAML, BrokerConfig.class);

        assertEquals("localhost", conf.getHost());
        assertEquals(1883, conf.getPort());
        assertEquals(new AuthConfig("fred", "changeit"), conf.getAuth());

    }

    @Test
    public void parseBrokerConfigTls() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        BrokerConfig conf = om.readValue(brokerConfigTlsYAML, BrokerConfig.class);

        System.out.println("DEBUG conf " + conf);

    }

    @Test
    public void parseTLSConfig() throws JsonProcessingException {

        final String configYaml = "---\n" +
          "trustStore: \"teststore.jks\"\n" +
          "trustPass: \"changeit\"";


        TLSConfig config = new TLSConfig("blbstore.jks", "password".toCharArray());
        ObjectMapper omy = new ObjectMapper(new YAMLFactory());

        TLSConfig parsedConf = omy.readValue(configYaml, TLSConfig.class);

        System.out.println("DEBUG parsedConf.trustStore " + parsedConf.getTrustStore());
        System.out.println("DEBUG parsedConf.trustPass " + new String(parsedConf.getTrustPass()));

        ObjectWriter owy = omy.writer();

        System.out.println("DEBUG config\n" + owy.writeValueAsString(config));

    }

}
