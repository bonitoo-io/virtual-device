package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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

        assertEquals("myTrustStore.jks", conf.getTls().getTrustStore());
        assertEquals("foobar", new String(conf.getTls().getTrustPass()));

    }

    @Test
    public void parseTLSConfig() throws JsonProcessingException {

        final String configYaml = "---\n" +
          "trustStore: \"teststore.jks\"\n" +
          "trustPass: \"changeit\"";


        TlsConfig config = new TlsConfig("blbstore.jks", "password".toCharArray());
        ObjectMapper omy = new ObjectMapper(new YAMLFactory());

        TlsConfig parsedConf = omy.readValue(configYaml, TlsConfig.class);

        assertEquals("teststore.jks", parsedConf.getTrustStore());

        assertEquals("changeit", new String(parsedConf.getTrustPass()));

        ObjectWriter owy = omy.writer();

        TlsConfig mirrorConf = omy.readValue(owy.writeValueAsString(config), TlsConfig.class);

        assertEquals("blbstore.jks", mirrorConf.getTrustStore());
        assertEquals("password", new String(mirrorConf.getTrustPass()));
    }

    @Test
    @Tag("envars")
    public void parseTLSConfigWithENVARS() throws JsonProcessingException {

        final String configYaml = "---\n" +
          "trustStore: \"teststore.jks\"\n" +
          "trustPass: \"changeit\"";

        ObjectMapper omy = new ObjectMapper(new YAMLFactory());

        TlsConfig parsedConf = omy.readValue(configYaml, TlsConfig.class);

        String ENV_TRUSTSTORE = System.getenv("VD_TRUSTSTORE");
        String ENV_TRUSTSTORE_PASSWORD = System.getenv("VD_TRUSTSTORE_PASSWORD");

        if(ENV_TRUSTSTORE != null) {
            assertEquals(ENV_TRUSTSTORE, parsedConf.getTrustStore());
        } else {
            assertEquals("teststore.jks", parsedConf.getTrustStore());
        }

        if(ENV_TRUSTSTORE_PASSWORD != null) {
            assertEquals(ENV_TRUSTSTORE_PASSWORD, new String(parsedConf.getTrustPass()));
        } else {
            assertEquals("changeit", new String(parsedConf.getTrustPass()));
        }

    }


}
