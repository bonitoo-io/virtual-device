package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.util.EncryptPass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

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

  // TODO implement encrypted passwords.
  char[] password;

  public char[] getPassword(){
    if (EncryptPass.passIsEncoded(password)) {
      return EncryptPass.decryptPass(
        TlsConfig.class.getPackage().getName().toCharArray(),
        new String(password)
      );
    }
    return password;
  }

  public char[] getRawPassword(){
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
