package io.bonitoo.qa.data.generator;

public class SimpleStringGenerator extends DataGenerator {

  @Override
  public String genData(Object... args) {
    return (String)args[((int) Math.floor(Math.random() * args.length))];
  }

}
