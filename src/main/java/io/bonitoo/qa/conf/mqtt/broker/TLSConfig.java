package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.util.CryptoHelper;
import io.bonitoo.qa.util.EncryptPass;
import io.bonitoo.qa.util.LogHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.regex.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = TLSConfigDeserializer.class)
public class TLSConfig {

  @JsonIgnore
  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @JsonIgnore
  private static String ENCODE_HEADER = "ENC";

  String trustStore;

  char[] trustPass;

  public char[] getTrustPass() {
    if (EncryptPass.passIsEncoded(trustPass)) {
      try {
        return EncryptPass.decryptTrustPass(TLSConfig.class.getPackage().getName(),
          TLSConfig.class.getSimpleName(),
          new String(trustPass));
      } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
               | InvalidAlgorithmParameterException | InvalidKeyException
               | IllegalBlockSizeException | BadPaddingException e) {
        throw new RuntimeException(e);
      }
    }
    return trustPass;
  }
  @Override
  public String toString() {
    return String.format("trustStore: %s, trustPass: %s", trustStore, new String(trustPass));
  }

}
