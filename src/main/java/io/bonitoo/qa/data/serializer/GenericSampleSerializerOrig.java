package io.bonitoo.qa.data.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.bonitoo.qa.data.GenericSample;
import io.bonitoo.qa.data.Item;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A serializer for samples based on the Generic Sample class.
 */
public class GenericSampleSerializerOrig extends StdSerializer<GenericSample> {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public GenericSampleSerializerOrig() {
    this(null);
  }

  protected GenericSampleSerializerOrig(Class<GenericSample> t) {
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
    //  Item it = genericSample.getItems().get(key);
      for(Item it : genericSample.getItems().get(key)){
        serProvider.defaultSerializeField(it.getLabel(), it, jsonGen);
      }
      // jsonGen.writeFieldName(it.getLabel());
     // serProvider.defaultSerializeField(it.getLabel(), it, jsonGen);
    }
    jsonGen.writeEndObject();

  }
}
