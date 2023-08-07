package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.Constants;
import io.bonitoo.qa.conf.VirDevDeserializer;
import java.io.IOException;

/**
 * Deserializes node to AuthConfig instance.
 */
public class AuthConfigDeserializer extends VirDevDeserializer<AuthConfig> {

  public AuthConfigDeserializer() {
    this(null);
  }

  protected AuthConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public AuthConfig deserialize(JsonParser jsonParser,
                                DeserializationContext ctx)
      throws IOException {

    String envUser = System.getenv(Constants.ENV_BROKER_USER);
    char[] envPassword = System.getenv(Constants.ENV_BROKER_PASSWORD) == null ? null
      : System.getenv(Constants.ENV_BROKER_PASSWORD).toCharArray();

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String username = envUser == null ? node.get("username").asText()
        : envUser;
    char[] password = envPassword == null ? node.get("password").asText().toCharArray()
        : envPassword;
    return new AuthConfig(username, password);
  }
}
