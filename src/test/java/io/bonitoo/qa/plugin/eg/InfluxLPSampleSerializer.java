package io.bonitoo.qa.plugin.eg;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.bonitoo.qa.data.Item;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class InfluxLPSampleSerializer extends StdSerializer<InfluxLPSamplePlugin> {

  static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset

  static {
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  protected InfluxLPSampleSerializer(Class<InfluxLPSamplePlugin> t) {
    super(t);
  }

  public InfluxLPSampleSerializer() {
    this(null);
  }

  @Override
  public void serialize(InfluxLPSamplePlugin influxPlugin,
                        JsonGenerator jsonGen,
                        SerializerProvider serProvider) throws IOException {

    jsonGen.writeStartObject();
    jsonGen.writeStringField("measurement", influxPlugin.measurement);
    jsonGen.writeFieldName("tags");
    jsonGen.writeStartObject();
    for(String key : influxPlugin.getTags().keySet()){
      jsonGen.writeStringField(key, influxPlugin.getTags().get(key));
    }
    jsonGen.writeEndObject();
    jsonGen.writeFieldName("fields");
    jsonGen.writeStartObject();
    for(String key : influxPlugin.getItems().keySet()){
      Item it = influxPlugin.item(key);
      serProvider.defaultSerializeField(it.getLabel(), it, jsonGen);
    }
    jsonGen.writeEndObject();
    jsonGen.writeStringField("timestamp", df.format(new Date(influxPlugin.getTimestamp())));
    jsonGen.writeEndObject();

  }
}
