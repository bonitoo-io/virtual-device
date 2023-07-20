package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.Constants;
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

    String envTruststore = System.getenv(Constants.ENV_TLS_STORE);
    char[] envPassword = System.getenv(Constants.ENV_TLS_PASSWORD) == null ? null :
      System.getenv().get(Constants.ENV_TLS_PASSWORD).toCharArray();

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String trustStore = envTruststore == null
        ? safeGetNode(node, "trustStore").asText()
        : envTruststore;
    char[] password = envPassword == null
        ? safeGetNode(node, "trustPass").asText().toCharArray()
        : envPassword;

    return new TlsConfig(trustStore, password);
  }
}
