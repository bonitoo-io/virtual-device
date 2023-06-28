package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.VirDevDeserializer;

import java.io.IOException;

public class TLSConfigDeserializer extends VirDevDeserializer<TLSConfig> {

  public TLSConfigDeserializer() {
    this(null);
  }
  protected TLSConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public TLSConfig deserialize(JsonParser jsonParser,
                               DeserializationContext deserializationContext)
      throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String trustStore = safeGetNode(node, "trustStore").asText();
    char[] password = safeGetNode(node, "trustPass").asText().toCharArray();
    return new TLSConfig(trustStore, password);
  }
}
