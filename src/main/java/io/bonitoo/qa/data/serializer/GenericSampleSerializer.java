package io.bonitoo.qa.data.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.data.ItemArType;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.data.Item;
import java.io.IOException;

/**
 * A serializer for samples based on the Generic Sample class.
 */
public class GenericSampleSerializer extends StdSerializer<GenericSample> {

  public GenericSampleSerializer() {
    this(null);
  }

  protected GenericSampleSerializer(Class<GenericSample> t) {
    super(t);
  }

  @Override
  public void serialize(GenericSample gs, JsonGenerator jsonGen, SerializerProvider serProvider) throws IOException {

    jsonGen.writeStartObject();
    jsonGen.writeStringField("id", gs.getId());
    jsonGen.writeNumberField("timestamp", gs.getTimestamp());
    for (String key : gs.getItems().keySet()) {
      if (gs.getItems().get(key).size() == 1) {
        Item item = gs.getItems().get(key).get(0);
        serProvider.defaultSerializeField(item.getLabel(), item, jsonGen);
      } else {
        ItemArType arType = gs.getItems().get(key).get(0).getConfig().getArType();
        switch (arType) {
          case Array:
            jsonGen.writeFieldName(gs.getItems().get(key).get(0).getLabel());
            jsonGen.writeStartArray();
            break;
          case Object:
            jsonGen.writeFieldName(gs.getItems().get(key).get(0).getLabel());
            jsonGen.writeStartObject();
            break;
          case Undefined:
          case Flat:
            // nothing to do
            break;
          default:
            throw new VirtualDeviceRuntimeException("Unhandled ItemArrayType "
              + arType);
        }
        for (Item it : gs.getItems().get(key)) {
          String labelFormat = "%s%0" + (((int) Math.ceil(Math.log10(gs.getItems().get(key).size()))) + 1) + "d";
          int index = gs.getItems().get(key).indexOf(it);
          switch (arType) {
            case Flat:
            case Undefined:
              serProvider.defaultSerializeField(
                  String.format(labelFormat, it.getLabel(), index), it, jsonGen);
              break;
            case Array:
              serProvider.defaultSerializeValue(it, jsonGen);
              break;
            case Object:
              serProvider.defaultSerializeField(
                  String.format(labelFormat, "", index), it, jsonGen
              );
              break;
            default:
              throw new VirtualDeviceRuntimeException("Unhandled ItemArrayType "
                + arType);
          }
        }
        switch (arType) {
          case Array:
            jsonGen.writeEndArray();
            break;
          case Object:
            jsonGen.writeEndObject();
            break;
          case Undefined:
          case Flat:
            // nothing to do.
            break;
          default:
            throw new VirtualDeviceRuntimeException("Unhandled ItemArrayType "
              + arType);
        }
      }

    }
    jsonGen.writeEndObject();
  }
}
