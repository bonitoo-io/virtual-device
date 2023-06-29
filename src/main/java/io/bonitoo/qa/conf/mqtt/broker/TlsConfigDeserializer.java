package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.VirDevDeserializer;
import java.io.IOException;

/**
 * Deserializer for TlsConfig.
 */
public class TlsConfigDeserializer extends VirDevDeserializer<TlsConfig> {

  public TlsConfigDeserializer() {
    this(null);
  }

  protected TlsConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public TlsConfig deserialize(JsonParser jsonParser,
                               DeserializationContext deserializationContext)
      throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String trustStore = safeGetNode(node, "trustStore").asText();
    char[] password = safeGetNode(node, "trustPass").asText().toCharArray();
    return new TlsConfig(trustStore, password);
  }
}
