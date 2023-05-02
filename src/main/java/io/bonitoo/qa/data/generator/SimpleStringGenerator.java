package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.conf.data.DataConfig;

/**
 * A simple generator that accepts and array of strings and randomly
 * selects one of them as the response.
 */
public class SimpleStringGenerator extends DataGenerator<DataConfig> {

  @Override
  public String genData(Object... args) {
    return (String) args[((int) Math.floor(Math.random() * args.length))];
  }

}
