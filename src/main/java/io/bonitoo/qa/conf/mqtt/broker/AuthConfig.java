package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.util.EncryptPass;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration for simple authorization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = AuthConfigDeserializer.class)
public class AuthConfig {

  String username;
  char[] password;

  /**
   * Special getter checks to see if held password value is encrypted.
   * If so it then decrypts it.
   *
   * @return - the password.
   */
  public char[] getPassword() {
    if (EncryptPass.passIsEncoded(password)) {
      return EncryptPass.decryptPass(
        TlsConfig.class.getPackage().getName().toCharArray(),
        new String(password)
      );
    }
    return password;
  }

  public char[] getRawPassword() {
    return password;
  }

  @Override
  public String toString() {
    return String.format("name=%s,password=%s",
      username,
      new String(password).replaceAll("/\\pL\\pN/", "*"));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof AuthConfig)) {
      return false;
    }

    final AuthConfig conf = (AuthConfig) obj;

    return conf.getUsername().equals(username)
      && Arrays.equals(conf.getPassword(), password);
  }

}
