package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.data.Sample;
import io.bonitoo.qa.data.serializer.GenericSampleSerializer;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/*
{
        "measurement": "temperature",
        "tags": {
            "sensor": "TMP36",
            "location": "3CR2+CJ"
        },
        "fields": {
            "temperature": 18.6,
            "pressure": 28.3
        },
        "timestamp": "2023-01-13T12:30:00Z"
    },
 */
@Getter
@Setter
@JsonSerialize(using = InfluxLPSampleSerializer.class)
public class InfluxLPSamplePlugin extends SamplePlugin {

  String measurement;

  Map<String, String> tags;

  // N.B. use items from parent sample for fields
  public InfluxLPSamplePlugin(PluginProperties props, SampleConfig config, String measurement, Map<String,String> tags) {
    super(props, config);
    this.measurement = measurement;
    this.tags = tags;
  }

  @Override
  public Sample update() {
    for(String itemName : this.getItems().keySet()){
      this.getItems().get(itemName).update();
    }
    this.timestamp = System.currentTimeMillis();
    return this;
  }

  @Override
  public void onLoad() {
    enabled = true;
  }

  @Override
  public String toJson() throws JsonProcessingException {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(this);
  }

}
