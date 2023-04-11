package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.VirDevDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A deserializer for a SampleConfig document.
 */
public class SampleConfigDeserializer extends VirDevDeserializer<SampleConfig> {

  public SampleConfigDeserializer() {
    this(null);
  }

  protected SampleConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public SampleConfig deserialize(JsonParser jsonParser,
                                  DeserializationContext ctx)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String name = safeGetNode(node, "name").asText();
    String id = safeGetNode(node, "id").asText();
    String topic = safeGetNode(node, "topic").asText();
    JsonNode itemsNode = safeGetNode(node, "items");
    List<ItemConfig> items = new ArrayList<>();

    for (JsonNode itemNode : itemsNode) {
      if (itemNode.isTextual()) {
        items.add(ItemConfigRegistry.get(itemNode.asText()));
      } else {
        items.add(ctx.readValue(itemNode.traverse(jsonParser.getCodec()), ItemConfig.class));
      }
    }

    return new SampleConfig(id, name, topic, items);
  }
}
