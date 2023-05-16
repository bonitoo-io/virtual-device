package io.bonitoo.qa.plugin.eg;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.data.SampleConfigDeserializer;
import io.bonitoo.qa.plugin.SamplePluginConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class InfluxLPSamplePluginConfDeserializer extends SampleConfigDeserializer {

  public InfluxLPSamplePluginConfDeserializer() {
    this(null);
  }

  protected InfluxLPSamplePluginConfDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public InfluxLPSamplePluginConf deserialize(JsonParser jsonParser,
                                  DeserializationContext ctx)
    throws IOException {

    TreeNode tn = jsonParser.readValueAsTree();

    SamplePluginConfig conf = jsonParser.getCodec().treeToValue(tn, SamplePluginConfig.class);

    String measurement = ((JsonNode)tn.get("measurement")).asText();
    TreeNode tags = tn.get("tags");
    Iterator<String> fields = tags.fieldNames();
    HashMap<String, String> map = new HashMap<>();
    while(fields.hasNext()){
      String field = fields.next();
      map.put(field, ((JsonNode)tags.get(field)).asText());
    }

    return new InfluxLPSamplePluginConf(conf,
       measurement, map);
  }

}
