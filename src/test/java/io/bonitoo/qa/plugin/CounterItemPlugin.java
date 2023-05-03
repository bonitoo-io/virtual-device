package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.conf.data.ItemConfig;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CounterItemPlugin extends ItemGenPlugin{

  AtomicLong counter = new AtomicLong(0);
  public CounterItemPlugin(PluginProperties props, ItemConfig config, boolean enabled) {
    super(props, config, enabled);
  }

  public CounterItemPlugin(){
    super();
  }

  @Override
  public Long getCurrentVal() {
    return counter.get();
  }

  @Override
  public void onLoad() {
    enabled = true;
  }

  @Override
  public Long genData(Object... args) {
    int delta = (args.length < 1 || args[0] == null) ? 1 : (Integer)args[0];
    return counter.addAndGet(delta);
  }
}