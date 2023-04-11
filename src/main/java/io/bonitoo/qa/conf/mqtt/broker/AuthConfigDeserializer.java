package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
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

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String username = node.get("username").asText();
    String password = node.get("password").asText();
    return new AuthConfig(username, password);
  }
}
