package io.bonitoo.qa.plugin.eg;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.conf.data.ItemPluginConfig;
import io.bonitoo.qa.plugin.PluginProperties;
import lombok.Getter;


@Getter
@JsonDeserialize(using = MovingAveragePluginConfDeserializer.class)
public class MovingAveragePluginConf extends ItemPluginConfig {

  protected static int defaultWindow = 10;

  @JsonSerialize
  @JsonDeserialize
  Integer window = defaultWindow;

  @JsonSerialize
  @JsonDeserialize
  Double min;

  @JsonSerialize
  @JsonDeserialize
  Double max;

 // public SampleAveragePluginConf(PluginProperties props, String name) {
 //   super(props, name);
 // }

  public MovingAveragePluginConf(PluginProperties props, String name) {
    super(props, name);
  }

  public MovingAveragePluginConf(ItemPluginConfig config) {
    super(config);
    if(config instanceof MovingAveragePluginConf) {
      this.window = ((MovingAveragePluginConf)config).getWindow();
      this.max = ((MovingAveragePluginConf)config).getMax();
      this.min = ((MovingAveragePluginConf)config).getMin();


    }
  }

  @Override
  public String toString(){
    return String.format("%s,window:%d,min:%f,max:%f",
      super.toString(),
      window,
      min,
      max);
  }
}
