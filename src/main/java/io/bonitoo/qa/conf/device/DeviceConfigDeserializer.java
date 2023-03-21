package io.bonitoo.qa.conf.device;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import io.bonitoo.qa.conf.device.DeviceConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceConfigDeserializer extends StdDeserializer<DeviceConfig> {

    // todo define default values in global config
    static final Long defaultInterval = 3000l;
    static final Long defaultJitter = 0l;

    static final int defaultCount = 1;

    public DeviceConfigDeserializer(){
        this(null);
    }

    protected DeviceConfigDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DeviceConfig deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException, JacksonException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String id = node.get("id").asText();
        String name = node.get("name").asText();
        String description = node.get("description").asText();
        Long interval = node.get("interval") == null ? defaultInterval : node.get("interval").asLong();
        Long jitter = node.get("jitter") == null ? defaultJitter : node.get("jitter").asLong();
        Integer count = node.get("count") == null ? defaultCount : node.get("count").asInt();
        JsonNode samplesNode = node.get("samples");
        List<SampleConfig> samples = new ArrayList<>();

        for(JsonNode sampleNode : samplesNode){
            if(sampleNode.isTextual()) {
                samples.add(SampleConfigRegistry.get(sampleNode.asText()));
            }else{
                samples.add(ctx.readValue(sampleNode.traverse(jsonParser.getCodec()), SampleConfig.class));
            }
        }

        return new DeviceConfig(id, name, description, samples, interval, jitter, count);
    }
}
