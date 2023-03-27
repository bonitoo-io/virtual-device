package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

/**
 * Deserializes a node to a BrokerConfig instance.
 */
public class BrokerConfigDeserializer extends StdDeserializer<BrokerConfig> {

  public BrokerConfigDeserializer() {
    this(null);
  }

  protected BrokerConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public BrokerConfig deserialize(JsonParser jsonParser,
                                  DeserializationContext ctx)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String host = node.get("host").asText();
    int port = node.get("port").asInt();
    JsonNode authNode = node.get("auth");
    AuthConfig authConf = ctx.readValue(authNode.traverse(jsonParser.getCodec()),
        AuthConfig.class);

    return new BrokerConfig(host, port, authConf);
  }
}
