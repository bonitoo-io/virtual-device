package io.bonitoo.qa.conf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RunnerConfigDeserializer extends StdDeserializer<RunnerConfig> {

    public RunnerConfigDeserializer(){
        this(null);
    }

    protected RunnerConfigDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public RunnerConfig deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException, VirtualDeviceConfigException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        JsonNode ttlNode = node.get("ttl");
        JsonNode brokerNode = node.get("broker");
        JsonNode itemsNode = node.get("items");
        JsonNode samplesNode = node.get("samples");
        JsonNode devicesNode = node.get("devices");

        if(ttlNode == null &&
            brokerNode == null &&
            itemsNode == null &&
            samplesNode == null &&
            devicesNode == null){
            throw new VirtualDeviceConfigException("RunnerConfig source contains no nodes to be serialized");
        }


        Long ttl = ttlNode == null ? Long.parseLong(Config.getProp("default.ttl")) : ttlNode.asLong();

        BrokerConfig broker = brokerNode == null ? new BrokerConfig(Config.getProp("default.broker.host"),
                Integer.parseInt(Config.getProp("default.broker.port")), null) :
                ctx.readValue(brokerNode.traverse(jsonParser.getCodec()), BrokerConfig.class);

        if(itemsNode != null) {
            for (JsonNode itemNode : itemsNode) {
                if (itemNode.isTextual()) {
                    throw new RuntimeException("Top level item node must define item config object.");
                }
                ItemConfig itemConf = ctx.readValue(itemNode.traverse(jsonParser.getCodec()), ItemConfig.class);
                ItemConfigRegistry.add(itemConf.getName(), itemConf);
            }
        }

        if(samplesNode != null) {
            for (JsonNode sampleNode : samplesNode) {
                if (sampleNode.isTextual()) {
                    throw new RuntimeException("Top level sample node must define sample config object");
                }
                SampleConfig sampleConf = ctx.readValue(sampleNode.traverse(jsonParser.getCodec()), SampleConfig.class);
                SampleConfigRegistry.add(sampleConf.getName(), sampleConf);
            }
        }

        List<DeviceConfig> devices = new ArrayList<>();

        if(devicesNode != null) {
            for (JsonNode deviceNode : devicesNode) {
                devices.add(ctx.readValue(deviceNode.traverse(jsonParser.getCodec()), DeviceConfig.class));
            }
        }

        return new RunnerConfig(broker, devices, ttl);
    }
}