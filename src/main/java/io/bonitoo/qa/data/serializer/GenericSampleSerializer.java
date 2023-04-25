package io.bonitoo.qa.data.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.data.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class GenericSampleSerializer extends StdSerializer<GenericSample> {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public GenericSampleSerializer() {
    this(null);
  }

  protected GenericSampleSerializer(Class<GenericSample> t) {
    super(t);
  }

  // id, timestamp, items
  @Override
  public void serialize(GenericSample genericSample,
                        JsonGenerator jsonGen,
                        SerializerProvider serProvider) throws IOException {

    jsonGen.writeStartObject();
    jsonGen.writeStringField("id", genericSample.getId());
    jsonGen.writeNumberField("timestamp", genericSample.getTimestamp());
    for (String key : genericSample.getItems().keySet()) {
      Item it = genericSample.getItems().get(key);
      // jsonGen.writeFieldName(it.getLabel());
      serProvider.defaultSerializeField(it.getLabel(), it, jsonGen);
    }
    jsonGen.writeEndObject();

  }
}
