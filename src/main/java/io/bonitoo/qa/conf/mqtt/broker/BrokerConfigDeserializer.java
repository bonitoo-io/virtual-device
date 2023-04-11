package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.VirDevDeserializer;
import java.io.IOException;

/**
 * Deserializes a node to a BrokerConfig instance.
 */
public class BrokerConfigDeserializer extends VirDevDeserializer<BrokerConfig> {

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
    String host = safeGetNode(node, "host").asText();
    int port = safeGetNode(node, "port").asInt();
    JsonNode authNode = safeGetNode(node, "auth");
    AuthConfig authConf = ctx.readValue(authNode.traverse(jsonParser.getCodec()),
        AuthConfig.class);

    return new BrokerConfig(host, port, authConf);
  }
}
