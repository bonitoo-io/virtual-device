package io.bonitoo.qa.plugin.eg;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.data.ItemConfigDeserializer;
import io.bonitoo.qa.conf.data.ItemPluginConfig;

import java.io.IOException;

public class MovingAveragePluginConfDeserializer extends ItemConfigDeserializer {

  @Override
  public MovingAveragePluginConf deserialize(JsonParser jsonParser,
                                             DeserializationContext ctx)
    throws IOException {

    TreeNode tn = jsonParser.readValueAsTree();

    ItemPluginConfig itemPluginConfig = jsonParser
      .getCodec()
      .treeToValue(tn, ItemPluginConfig.class);

    MovingAveragePluginConf conf = new MovingAveragePluginConf(itemPluginConfig);

    TreeNode windowNode = tn.get("window");
    if (windowNode != null) {
      conf.window = ((JsonNode)windowNode).asInt();
    }

    TreeNode minNode = tn.get("min");
    if( minNode != null) {
      conf.min = ((JsonNode)minNode).asDouble();
    }

    TreeNode maxNode = tn.get("max");
    if( maxNode != null) {
      conf.max = ((JsonNode)maxNode).asDouble();
    }

    return conf;
  }
}
