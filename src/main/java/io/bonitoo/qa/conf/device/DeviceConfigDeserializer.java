package io.bonitoo.qa.conf.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.VirDevDeserializer;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import io.bonitoo.qa.plugin.sample.SamplePlugin;
import io.bonitoo.qa.plugin.sample.SamplePluginConfig;
import io.bonitoo.qa.plugin.sample.SamplePluginConfigClass;
import io.bonitoo.qa.plugin.sample.SamplePluginMill;
import io.bonitoo.qa.util.LogHelper;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Deserializer to DeviceConfig instances.
 */
public class DeviceConfigDeserializer extends VirDevDeserializer<DeviceConfig> {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    String id = safeGetNode(node, "id").asText();
    String name = safeGetNode(node, "name").asText();
    String description = safeGetNode(node, "description").asText();
    Long interval = node.get("interval") == null ? defaultInterval : node.get("interval").asLong();
    Long jitter = node.get("jitter") == null ? defaultJitter : node.get("jitter").asLong();
    int count = node.get("count") == null ? defaultCount : node.get("count").asInt();
    JsonNode samplesNode = safeGetNode(node, "samples");
    List<SampleConfig> samples = new ArrayList<>();

    for (JsonNode sampleNode : samplesNode) {
      if (sampleNode == null) {
        throw new VirDevConfigException("Encountered null sampleNode.  "
          + "Cannot continue deserialization of device " + name);
      }
      if (sampleNode.isTextual()) {
        samples.add(SampleConfigRegistry.get(sampleNode.asText()));
      } else {
        JsonNode pluginNode = sampleNode.get("plugin");
        if (pluginNode instanceof NullNode || pluginNode == null) { // not a plugin based sample
          samples.add(ctx.readValue(sampleNode.traverse(jsonParser.getCodec()),
              SampleConfig.class));
        } else {
          String pluginName = pluginNode.asText();
          @SuppressWarnings("unchecked")
          Class<? extends SamplePlugin> clazz =
              (Class<? extends SamplePlugin>) SamplePluginMill.getPluginClass(pluginName);
          Class<? extends SamplePluginConfig> confClazz = SamplePluginConfig.class;
          Annotation[] annotations = clazz.getAnnotations();
          for (Annotation a : annotations) {
            if (a instanceof SamplePluginConfigClass) {
              logger.info(LogHelper.buildMsg("", "DeserializeConf",
                  String.format("using %s class for deserializing sample config.",
                    ((SamplePluginConfigClass) a).conf().getName())
              ));
              confClazz = ((SamplePluginConfigClass) a).conf();
            }
          }
          samples.add(ctx.readValue(sampleNode.traverse(jsonParser.getCodec()), confClazz));
        }
      }
    }

    return new DeviceConfig(id, name, description, samples, interval, jitter, count);
  }
}
