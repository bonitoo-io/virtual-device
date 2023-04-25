package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.conf.data.DataConfig;
import java.lang.reflect.InvocationTargetException;

public abstract class DataGenerator<T extends DataConfig> {

  public abstract Object genData(Object... args);

  public static DataGenerator<? extends DataConfig> create(String className, Object... args) {
    ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
    try {
      @SuppressWarnings("unchecked")
      Class<DataGenerator<? extends DataConfig>> clazz =
          (Class<DataGenerator<? extends DataConfig>>) systemLoader.loadClass(className);
      return clazz.getDeclaredConstructor().newInstance(args);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
             | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    // return null;
  }

}
