package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.data.Sample;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

@Setter
@Getter
public class SamplePlugin extends Sample implements Plugin {

  @JsonIgnore
  protected PluginProperties props;

  @JsonIgnore
  protected boolean enabled = false;

  public SamplePlugin(PluginProperties props, SampleConfig config){
    this.props = props;
    this.id = config.getId();
    this.topic = config.getTopic();
    this.items = new HashMap<>();
    for (ItemConfig itemConfig : config.getItems()) {
      this.items.put(itemConfig.getName(), Item.of(itemConfig));
    }
    this.timestamp = System.currentTimeMillis();
  }


/*  public static SamplePlugin of(PluginProperties props, SampleConfig config){
    SamplePlugin sp = new SamplePlugin();
    sp.setProps(props);
    sp.setId(config.getId());
    sp.setTopic(config.getTopic());
    sp.setTimestamp(System.currentTimeMillis());
    sp.items = new HashMap<>();
    for (ItemConfig itemConfig : config.getItems()) {
      sp.getItems().put(itemConfig.getName(), Item.of(itemConfig));
    }

    return sp;
  } */

  @Override
  public void onLoad() {
    enabled = true;
  }

  public boolean onEnable() {
    return enabled = true;
  }

  public boolean onDisable() {
    return enabled = false;
  }

  @Override
  public Sample update() {
    return this;
  }

  @Override
  public String toJson() throws JsonProcessingException {
    return null;
  }
}
