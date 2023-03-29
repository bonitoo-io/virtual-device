package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.bonitoo.qa.conf.VirtualDeviceConfigException;
import io.bonitoo.qa.data.ItemType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Deserializes YAML configuration node of an item configuration into
 * an ItemConfig object.
 */
public class ItemConfigDeserializer extends StdDeserializer<ItemConfig> {

  public ItemConfigDeserializer() {
    this(null);
  }

  protected ItemConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  private static JsonNode safeGetNode(JsonNode node,
                                      String subName)
      throws VirtualDeviceConfigException {
    JsonNode subNode = node.get(subName);
    if (subNode == null) {
      throw new VirtualDeviceConfigException(
        String.format("property \"%s\" for node %s is null.  Cannot parse any further",
          subName, node)
      );
    }
    return subNode;
  }

  @Override
  public ItemConfig deserialize(JsonParser jsonParser,
                                DeserializationContext deserializationContext)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    ItemType type = ItemType.valueOf(node.get("type").asText());
    String name = node.get("name").asText();
    Object max;
    Object min;
    long period;
    List<String> vals;

    switch (type) {
      case Double:
        max = safeGetNode(node, "max").asDouble();
        min = safeGetNode(node, "min").asDouble();
        period = safeGetNode(node, "period").asLong();
        return new ItemNumConfig(name, type, (Double) min, (Double) max, period);
      case Long:
        max = safeGetNode(node, "max").asLong();
        min = safeGetNode(node, "min").asLong();
        period = safeGetNode(node, "period").asLong();
        return new ItemNumConfig(name, type, (Long) min, (Long) max, period);
      case String:
        vals = new ArrayList<>();
        for (Iterator<JsonNode> it = safeGetNode(node, "values").elements(); it.hasNext(); ) {
          JsonNode elem = it.next();
          vals.add(elem.asText());
        }
        return new ItemStringConfig(name, type, vals);
      default:
        throw new RuntimeException(String.format("Unhandled Item Type %s", type));
    }

  }
}
