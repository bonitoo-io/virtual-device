package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.Item;
import io.bonitoo.qa.data.Sample;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * The basis for Sample Plugins.
 */

@Setter
@Getter
public abstract class SamplePlugin extends Sample implements Plugin {

  @JsonIgnore
  protected PluginProperties props;

  @JsonIgnore
  protected boolean enabled = false;

  /**
   * Constructs a Sample Plugin.
   *
   * @param props - plugin properties.
   * @param config - configuration for the generated sample.
   */
  public SamplePlugin(PluginProperties props, SampleConfig config) {
    this.props = props;
    this.id = config.getId();
    this.topic = config.getTopic();
    this.items = new HashMap<>();
    for (ItemConfig itemConfig : config.getItems()) {
      this.items.put(itemConfig.getName(), Item.of(itemConfig));
    }
    this.timestamp = System.currentTimeMillis();
  }

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
    this.timestamp = System.currentTimeMillis();
    return this;
  }

  @Override
  public abstract String toJson() throws JsonProcessingException; /* {
    return null;
  } */
}
