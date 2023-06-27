package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Test
    public void checkTLSEncodedPassword(){

        assertTrue(TLSConfig.passIsEncoded("ENCABCD".toCharArray()));
        assertTrue(TLSConfig.passIsEncoded("ENCABCD123=".toCharArray()));
        assertTrue(TLSConfig.passIsEncoded("ENCAbcd1234ef==".toCharArray()));
        assertFalse(TLSConfig.passIsEncoded("ABCD".toCharArray()));
        assertFalse(TLSConfig.passIsEncoded("ENCA12".toCharArray()));

    }

    @Test
    public void verifyEncryption() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException {

        final String testPass1 = "changeit";
        final String testHash = "ENCAAAAEC2O8a/ExCXKNPYn2JQPS7AougX5958+aa5wOjyN0+5i";

        System.out.println("DEBUG testPass1 " + testPass1);

        String passHash = TLSConfig.encryptTrustPass(
          this.getClass().getPackage().getName(),
          this.getClass().getSimpleName(), testPass1.toCharArray());

        System.out.println("DEBUG pass " + passHash);

       // char[] decrypted = TLSConfig.decryptTrustPass(this.getClass().getPackage().getName(), pass);
        char [] password = TLSConfig.decryptTrustPass(
          this.getClass().getPackage().getName(),
          this.getClass().getSimpleName(),
          passHash
        );

        System.out.println("DEBUG password " + new String(password));
        assertEquals(testPass1, new String(password));

        char[] testHashPass = TLSConfig.decryptTrustPass(
          this.getClass().getPackage().getName(),
          this.getClass().getSimpleName(),
          testHash
        );

        System.out.println("DEBUG testHashPass " + new String(testHashPass));
        assertEquals(testPass1, new String(testHashPass));

    }

}
