package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.Constants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
        assertArrayEquals("changeit".toCharArray(), conf.getPassword());

    }

    @Test
    public void parseAuthConfigPassEncrypted() throws JsonProcessingException {
        String authConfigYamlEncrypted = "---\n" +
          "username: musashi\n" +
          "password: ENC0r9OaRpVBRAzsYhIsALU9w8EThXxlJT/YvFx64fkohEAAAAQm0CcLMFtAjEX4abdpEc2+jKb5r8XH2vMgnTPBDAl+mI=";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        AuthConfig conf = om.readValue(authConfigYamlEncrypted, AuthConfig.class);

        System.out.println("DEBUG conf.username " + conf.username);
        System.out.println("DEBUG conf.password " + new String(conf.password));
        System.out.println("DEBUG conf.getPassword " + new String(conf.getPassword()));

        assertEquals("musashi", conf.getUsername());
        assertEquals("ENC0r9OaRpVBRAzsYhIsALU9w8EThXxlJT/YvFx64fkohEAAAAQm0CcLMFtAjEX4abdpEc2+jKb5r8XH2vMgnTPBDAl+mI=",
          new String(conf.password));
        assertEquals("changeit", new String(conf.getPassword()));

    }

    @Test
    @Tag("envars")
    public void parseAuthConfigEnvVars() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        AuthConfig conf = om.readValue(authConfigYaml, AuthConfig.class);

        String envUsername = System.getenv(Constants.ENV_BROKER_USER);
        String envPassword = System.getenv(Constants.ENV_BROKER_PASSWORD);

        if (envUsername != null) {
            assertEquals(envUsername, conf.getUsername());
        } else {
            assertEquals("musashi", conf.getUsername());
        }

        if (envPassword != null) {
            assertEquals(envPassword, new String(conf.getRawPassword()));
        } else {
            assertEquals("changeit", new String(conf.getPassword()));
        }
    }

    @Test
    public void parseBrokerConfigBasic() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        BrokerConfig conf = om.readValue(brokerConfigYAML, BrokerConfig.class);

        assertEquals("localhost", conf.getHost());
        assertEquals(1883, conf.getPort());
        assertEquals(new AuthConfig("fred", "changeit".toCharArray()), conf.getAuth());

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

        String ENV_TRUSTSTORE = System.getenv(Constants.ENV_TLS_STORE);
        String ENV_TRUSTSTORE_PASSWORD = System.getenv(Constants.ENV_TLS_PASSWORD);

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
