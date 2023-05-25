package io.bonitoo.qa.plugin.sample;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigDeserializer;
import java.io.IOException;

/**
 * Deserializer for SamplePluginConfig.
 */
public class SamplePluginConfigDeserializer extends SampleConfigDeserializer {

  public SamplePluginConfigDeserializer() {
    this(null);
  }

  protected SamplePluginConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  /**
   * Method called by Jackson to deserialize a SamplePluginConfig.
   *
   * @param jsonParser -
   * @param ctx -
   * @return - a new SamplePluginConfig
   * @throws IOException -
   */
  public SamplePluginConfig deserialize(JsonParser jsonParser,
                                              DeserializationContext ctx)
      throws IOException {

    TreeNode tn = jsonParser.readValueAsTree();
    SampleConfig sc = jsonParser.getCodec().treeToValue(tn, SampleConfig.class);

    return new SamplePluginConfig(sc);

  }
}
