package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.util.EncryptPass;
import java.lang.invoke.MethodHandles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for a TLS connection.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = TlsConfigDeserializer.class)
public class TlsConfig {

  @JsonIgnore
  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @JsonIgnore
  private static String ENCODE_HEADER = "ENC";

  String trustStore;

  char[] trustPass;

  /**
   * Gets the password as a char[].
   * If the password is encrypted, decrypts it.
   *
   * @return - the password.
   */
  public char[] getTrustPass() {
    if (EncryptPass.passIsEncoded(trustPass)) {
      return EncryptPass.decryptTrustPass(TlsConfig.class.getPackage().getName(),
          new String(trustPass));
    }
    return trustPass;
  }

  @Override
  public String toString() {
    return String.format("trustStore: %s, trustPass: %s", trustStore, new String(trustPass));
  }

}
