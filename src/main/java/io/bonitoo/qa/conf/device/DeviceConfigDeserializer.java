package io.bonitoo.qa.conf.device;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.VDevConfigException;
import io.bonitoo.qa.conf.VDevDeserializer;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Deserializer to DeviceConfig instances.
 */
public class DeviceConfigDeserializer extends VDevDeserializer<DeviceConfig> {

  static final Long defaultInterval = Long.parseLong(Config.getProp("default.device.interval"));
  static final Long defaultJitter = Long.parseLong(Config.getProp("default.device.jitter"));

  static final int defaultCount = Integer.parseInt(Config.getProp("default.device.count"));

  public DeviceConfigDeserializer() {
    this(null);
  }

  protected DeviceConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public DeviceConfig deserialize(JsonParser jsonParser,
                                  DeserializationContext ctx)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String id = safeGetNode(node,"id").asText();
    String name = safeGetNode(node,"name").asText();
    String description = safeGetNode(node,"description").asText();
    Long interval = node.get("interval") == null ? defaultInterval : node.get("interval").asLong();
    Long jitter = node.get("jitter") == null ? defaultJitter : node.get("jitter").asLong();
    int count = node.get("count") == null ? defaultCount : node.get("count").asInt();
    JsonNode samplesNode = safeGetNode(node,"samples");
    List<SampleConfig> samples = new ArrayList<>();

    for (JsonNode sampleNode : samplesNode) {
      if(sampleNode == null){
        throw new VDevConfigException("Encountered null sampleNode.  " +
          "Cannot continue deserialization of device " + name);
      }
      if (sampleNode.isTextual()) {
        samples.add(SampleConfigRegistry.get(sampleNode.asText()));
      } else {
        samples.add(ctx.readValue(sampleNode.traverse(jsonParser.getCodec()), SampleConfig.class));
      }
    }

    return new DeviceConfig(id, name, description, samples, interval, jitter, count);
  }
}
