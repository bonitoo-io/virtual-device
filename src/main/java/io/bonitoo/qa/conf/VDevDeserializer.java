package io.bonitoo.qa.conf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public abstract class VDevDeserializer<T> extends StdDeserializer<T> {
  protected VDevDeserializer(Class<?> vc) {
    super(vc);
  }

  protected static JsonNode safeGetNode(JsonNode node,
                                      String subName)
    throws VDevConfigException {
    JsonNode subNode = node.get(subName);
    if (subNode == null) {
      throw new VDevConfigException(
        String.format("property \"%s\" for node %s is null.  Cannot parse any further",
          subName, node)
      );
    }
    return subNode;
  }
}
