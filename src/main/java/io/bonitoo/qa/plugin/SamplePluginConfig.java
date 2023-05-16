package io.bonitoo.qa.plugin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.SampleConfig;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *  Configuration for a SampleConfig.  To be extended
 *  in custom SamplePlugins, wherever necessary.
 */
@AllArgsConstructor
@Getter
@Setter
@JsonDeserialize(using = SamplePluginConfigDeserializer.class)
public class SamplePluginConfig extends SampleConfig {
  public SamplePluginConfig(String id,
                            String name,
                            String topic,
                            List<ItemConfig> items,
                            String plugin) {
    super(id, name, topic, items, plugin);
  }

  public SamplePluginConfig(SamplePluginConfig conf) {
    super(conf);
  }

  public SamplePluginConfig(SampleConfig conf) {
    super(conf);
  }
}
