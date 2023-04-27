package io.bonitoo.qa.data.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.data.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class ItemSerializer extends StdSerializer<Item> {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public ItemSerializer() {
    this(null);
  }

  public ItemSerializer(Class<Item> t) {
    super(t);
  }

  @Override
  public void serialize(Item item, JsonGenerator jsonGen, SerializerProvider serializerProvider) throws IOException {
    switch (item.getConfig().getType()) {
      case BuiltInTemp:
        jsonGen.writeNumber(item.asDouble());
        break;
      case Double:
        Integer dprec = ((ItemNumConfig)item.getConfig()).getPrec();
        if (dprec != null) {
          jsonGen.writeNumber(Item.precision(item.asDouble(), dprec));
        } else {
          jsonGen.writeNumber(item.asDouble());
        }
        break;
      case Long:
        jsonGen.writeNumber(item.asLong());
        break;
      case String:
        jsonGen.writeString(item.asString());
        break;
      case Plugin:
        switch (((ItemPluginConfig) item.getConfig()).getResultType()) {
          case Double:
            Integer pprec = ((ItemPluginConfig)item.getConfig()).getPrec();
            if (pprec != null) {
              jsonGen.writeNumber(Item.precision(item.asDouble(), pprec));
            } else {
              jsonGen.writeNumber(item.asDouble());
            }
            break;
          case Long:
            jsonGen.writeNumber(item.asLong());
            break;
          case String:
            jsonGen.writeString(item.asString());
            break;
          case Json:
            logger.warn("PluginResultType.Json is not applicable to items. "
                + "Serialization ignored");
            break;
          default:
            throw new VirDevConfigException("Unknown PluginResultType "
              + ((ItemPluginConfig) item.getConfig()).getResultType());
        }
        break;
      default:
        throw new RuntimeException(String.format("Cannot deserialize unknown type %s ", item.getConfig().getType()));
    }
  }
}
