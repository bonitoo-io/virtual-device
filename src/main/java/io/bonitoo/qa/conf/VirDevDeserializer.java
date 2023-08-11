package io.bonitoo.qa.conf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
//      System.out.println("DEBUG subNode null using defVal " + defVal);
      ObjectNode tempNode = JsonNodeFactory.instance.objectNode();
      tempNode.put(subName, defVal);
//      System.out.println("DEBUG tempNode.asDouble " + tempNode.get(subName).asDouble());
      return tempNode.get(subName);
    }
    return subNode;
  }
}
