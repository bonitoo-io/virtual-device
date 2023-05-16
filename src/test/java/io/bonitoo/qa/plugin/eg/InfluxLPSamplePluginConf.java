package io.bonitoo.qa.plugin.eg;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.plugin.SamplePluginConfig;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@JsonDeserialize(using = InfluxLPSamplePluginConfDeserializer.class)
public class InfluxLPSamplePluginConf extends SamplePluginConfig {

  String measurement;

  Map<String, String> tags;

  public InfluxLPSamplePluginConf(String id,
                                  String name,
                                  String topic,
                                  List<ItemConfig> items,
                                  String measurement,
                                  Map<String,String> tags,
                                  String plugin){
    super(id, name, topic, items, plugin);
    this.measurement = measurement;
    this.tags = tags;
  }

  public InfluxLPSamplePluginConf(SamplePluginConfig conf, String measurement, Map<String,String> tags){
    super(conf);
    this.measurement = measurement;
    this.tags = tags;
  }

  @Override
  public String toString(){
    return String.format("%s,measurement:%s,tags%s", super.toString(), this.measurement, this.tags);
  }

}
