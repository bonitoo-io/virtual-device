package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import java.util.List;

/**
 * A simple generator that accepts and array of strings and randomly
 * selects one of them as the response.
 */
public class SimpleStringGenerator extends DataGenerator<DataConfig> {
  @Override
  public Object genData() {
    ItemStringConfig conf = (ItemStringConfig) item.getConfig();
    List<String> vals = conf.getValues();
    return vals.get(((int) Math.floor(Math.random() * vals.size())));
  }

}
