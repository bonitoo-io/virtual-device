package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.VirDevDeserializer;
import io.bonitoo.qa.data.GenericSample;
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

  private int getIndexOfItemConfigByName(List<ItemConfig> list, String name){

    for( int i = 0; i < list.size(); i++){
       if(list.get(i).getName().equals(name)){
         return i;
       }
    }
    return -1;
  }

  @Override
  public SampleConfig deserialize(JsonParser jsonParser,
                                  DeserializationContext ctx)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String name = safeGetNode(node, "name").asText();
    JsonNode pluginNode = node.get("plugin");
    String pluginName = pluginNode == null
        ? GenericSample.class.getName() :
        node.get("plugin").asText();
    String id = safeGetNode(node, "id").asText();
    String topic = safeGetNode(node, "topic").asText();
    ItemArType arType = ItemArType.valueOf(
        getDefaultStringNode(node, "arType", "Undefined").asText()
    );
    JsonNode itemsNode = safeGetNode(node, "items");
    List<ItemConfig> items = new ArrayList<>();

    for (JsonNode itemNode : itemsNode) {
      if (itemNode.isTextual()) {
        System.out.println("DEBUG detected textual");
        ItemConfig tic = ItemConfigRegistry.get(itemNode.asText());
        int ticIndex = getIndexOfItemConfigByName(items, tic.getName());
        if (ticIndex > -1) {
          items.get(ticIndex).setCount(items.get(ticIndex).getCount() + 1);
        } else {
          items.add(ItemConfigRegistry.get(itemNode.asText()));
        }
      } else {
        if (itemNode.has("from")) { // Using SampleItemNameConfig
          System.out.println("DEBUG detected field 'from'");
          String from = itemNode.get("from").asText();
          System.out.println("DEBUG getFrom " + from);
          int count = itemNode.get("count").asInt();
          System.out.println("DEBUG getCount " + itemNode.get("count").asInt());
          ItemConfig ic = ItemConfigRegistry.get(from);
          ic.count = count;
          // if arType not defined use default arType for Sample.
          ic.arType = ItemArType.valueOf(getDefaultStringNode(itemNode, "arType", arType.toString()).asText());
          items.add(ic);
        } else {
          //   SampleItemConfig siConfig = ctx.readValue(itemNode.traverse(jsonParser.getCodec()), SampleItemConfig.class);
          //    if (siConfig.count < 1) {
          //      throw new VirDevConfigException("Encountered a SampleItemConfig with a count less than 1");
          //    }

          //   if (siConfig.getItemConf() != null) {
          System.out.println("DEBUG detected raw ItemConfig");
          items.add(ctx.readValue(itemNode.traverse(jsonParser.getCodec()), ItemConfig.class));
          //   } else if(siConfig.getItemName() != null){
          // adds item from registry as above
          //   }
        }
      }
    }

    SampleConfig config = new SampleConfig(id, name, topic, items, pluginName);
    if (arType != ItemArType.Undefined) {
      config.setArType(arType);
    }

    return config;
  }
}
