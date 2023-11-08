package io.bonitoo.qa.conf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes a YAML runner configuration file to corresponding objects.
 */

public class RunnerConfigDeserializer extends VirDevDeserializer<RunnerConfig> {

  protected static Mode parseMode(String modeNode) {

    String cleanMode = modeNode.replace("\"", "");

    switch (cleanMode.toUpperCase()) {
      case "BLOCKING":
      case "BLOCK":
        return Mode.BLOCKING;
      case "REACTIVE":
      case "REACTIVEX":
      case "RX":
        return Mode.REACTIVE;
      case "ASYNC":
      case "ASYNCHRONOUS":
        return Mode.ASYNC;
      default:
        throw new VirDevConfigException(
          String.format("Unknown Mode type " + modeNode)
        );
    }
  }

  public RunnerConfigDeserializer() {
    this(null);
  }

  protected RunnerConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public RunnerConfig deserialize(JsonParser jsonParser, DeserializationContext ctx)
      throws IOException, VirDevConfigException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    if (node == null) {
      throw new VirDevConfigException(
        "RunnerConfig source contains no node to be serialized"
      );
    }

    JsonNode ttlNode = node.get("ttl");
    JsonNode brokerNode = node.get("broker");
    JsonNode itemsNode = node.get("items"); // can be null
    JsonNode samplesNode = node.get("samples"); // can be null
    JsonNode devicesNode = node.get("devices");
    // TODO node for mode - reactivex or blocking - default blocking
    JsonNode modeNode = node.get("mode"); // can be null

    if (ttlNode == null
        && brokerNode == null
        && itemsNode == null
        && samplesNode == null
        && devicesNode == null) {
      throw new VirDevConfigException(
        "RunnerConfig source contains no nodes to be serialized"
      );
    }

    final Long ttl = ttlNode == null
        ? Long.parseLong(Config.getProp("default.ttl"))
        : ttlNode.asLong();

    final BrokerConfig broker = brokerNode == null
        ? new BrokerConfig(Config.getProp("default.broker.host"),
        Integer.parseInt(Config.getProp("default.broker.port")), null) :
        ctx.readValue(brokerNode.traverse(jsonParser.getCodec()), BrokerConfig.class);

    final Mode mode = modeNode == null ? Mode.BLOCKING : parseMode(modeNode.toString());

    if (itemsNode != null) {
      for (JsonNode itemNode : itemsNode) {
        if (itemNode.isTextual()) {
          throw new RuntimeException("Top level item node must define item config object.");
        }
        ItemConfig itemConf = ctx.readValue(itemNode.traverse(jsonParser.getCodec()),
            ItemConfig.class);
        ItemConfigRegistry.add(itemConf.getName(), itemConf);
      }
    }

    if (samplesNode != null) {
      for (JsonNode sampleNode : samplesNode) {
        if (sampleNode.isTextual()) {
          throw new RuntimeException("Top level sample node must define sample config object");
        }
        SampleConfig sampleConf = ctx.readValue(sampleNode.traverse(jsonParser.getCodec()),
            SampleConfig.class);
        SampleConfigRegistry.add(sampleConf.getName(), sampleConf);
      }
    }

    List<DeviceConfig> devices = new ArrayList<>();

    if (devicesNode != null) {
      for (JsonNode deviceNode : devicesNode) {
        devices.add(ctx.readValue(deviceNode.traverse(jsonParser.getCodec()),
            DeviceConfig.class));
      }
    }

    return new RunnerConfig(broker, devices, ttl, mode);
  }
}
