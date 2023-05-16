package io.bonitoo.qa.plugin.eg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.Sample;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.SamplePlugin;
import io.bonitoo.qa.plugin.SamplePluginConfig;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
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

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  String measurement;

  Map<String, String> tags;

  public static InfluxLPSamplePlugin create(SamplePluginConfig conf){

    return new InfluxLPSamplePlugin(null,
      conf,
      ((InfluxLPSamplePluginConf)conf).getMeasurement(),
      ((InfluxLPSamplePluginConf)conf).getTags());
  }

  // N.B. use items from parent sample for fields
  public InfluxLPSamplePlugin(PluginProperties props, SamplePluginConfig config, String measurement, Map<String,String> tags) {
    super(props, config);
    this.measurement = measurement;
    this.tags = tags;
  }

  public InfluxLPSamplePlugin(PluginProperties props, SamplePluginConfig config, Object... args){
    super(props, config);
    this.measurement = (String)args[0];
    this.tags = (Map<String, String>) args[1];
  }

  @Override
  public Sample update() {
    super.update();
    for(String itemName : this.getItems().keySet()){
      this.getItems().get(itemName).update();
    }
    return this;
  }

  @Override
  public void onLoad() {
    enabled = true;
  }

  @Override
  public void applyProps(PluginProperties props) {
    // holder
    logger.info("applyProps called " + props.getMain());
  }

  @Override
  public String toJson() throws JsonProcessingException {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(this);
  }

  public static InfluxLPSamplePlugin create(SampleConfig sampleConfig) {
    return null;
  }
}
