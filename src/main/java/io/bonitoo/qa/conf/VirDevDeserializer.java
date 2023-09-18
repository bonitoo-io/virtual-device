package io.bonitoo.qa.conf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Base class for deserializers.
 *
 * <p>Includes static method <code>safeGetNode</code> to catch null parameters.
 *
 * @param <T> - Handles type to be serialized.
 */
public abstract class VirDevDeserializer<T> extends StdDeserializer<T> {
  protected VirDevDeserializer(Class<?> vc) {
    super(vc);
  }

  protected static JsonNode safeGetNode(JsonNode node,
                                        String subName)
      throws VirDevConfigException {
    JsonNode subNode = node.get(subName);
    if (subNode == null) {
      throw new VirDevConfigException(
        String.format("property \"%s\" for node %s is null.  Cannot parse any further",
          subName, node)
      );
    }
    return subNode;
  }

  protected static JsonNode getDefaultDoubleNode(JsonNode node,
                                        String subName,
                                        double defVal) {
    JsonNode subNode = node.get(subName);
    if (subNode == null) {
      return JsonNodeFactory.instance.numberNode(defVal);
    }
    return subNode;
  }

  protected static JsonNode getDefaultIntNode(JsonNode node,
                                                 String subName,
                                                 int defVal) {
    JsonNode subNode = node.get(subName);
    if (subNode == null) {
      return JsonNodeFactory.instance.numberNode(defVal);
    }
    return subNode;
  }

  protected static JsonNode getDefaultStringNode(JsonNode node,
                                              String subName,
                                              String defVal) {
    JsonNode subNode = node.get(subName);
    if (subNode == null) {
      return JsonNodeFactory.instance.textNode(defVal);
    }
    return subNode;
  }

}
