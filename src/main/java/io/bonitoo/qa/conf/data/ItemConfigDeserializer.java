package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.bonitoo.qa.data.ItemType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemConfigDeserializer  extends StdDeserializer<ItemConfig> {

    public ItemConfigDeserializer() {
        this(null);
    }

    protected ItemConfigDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ItemConfig deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        ItemType type = ItemType.valueOf(node.get("type").asText());
        String name = node.get("name").asText();
        Object max;
        Object min;
        long period;
        List<String> vals;

        switch (type) {
            case Double:
                max = node.get("max").asDouble();
                min = node.get("min").asDouble();
                period = node.get("period").asLong();
         //       ItemConfigRegistry.add(name, new ItemNumConfig(name, type, (Double)min, (Double)max, period));
                return new ItemNumConfig(name, type, (Double)min, (Double)max, period);
            case Long:
                max = node.get("max").asLong();
                min = node.get("min").asLong();
                period = node.get("period").asLong();
                //ItemConfigRegistry.add(name, new ItemNumConfig(name, type, (Long)min, (Long)max, period));
                return new ItemNumConfig(name, type, (Long)min, (Long)max, period);
            case String:
                vals = new ArrayList<>();
                for (Iterator<JsonNode> it = node.get("values").elements(); it.hasNext(); ) {
                    JsonNode elem = it.next();
                    vals.add(elem.asText());
                }
                return new ItemStringConfig(name, type, vals);
            default:
                throw new RuntimeException(String.format("Unhandled Item Type %s", type));
        }

    }
}



