package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.plugin.ItemPluginMill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

public abstract class DataGenerator<T extends DataConfig> {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public abstract Object genData(Object... args);

  public static DataGenerator<? extends DataConfig> create(String className, Object... args) {
    ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
    try {
      // check if plugin
      if (ItemPluginMill.hasPluginClass(className)) {
        System.out.println("IS PLUGIN");
        logger.info(String.format("Creating DataGenerator instance of plugin class %s", className));
        return ItemPluginMill.getPluginClassByName(className)
          .getDeclaredConstructor().newInstance(args);
      } else { // try systemLoader
        logger.info(String.format("Creating DataGenerator instance of internal class %s", className));
        @SuppressWarnings("unchecked")
        Class<DataGenerator<? extends DataConfig>> clazz =
          (Class<DataGenerator<? extends DataConfig>>) systemLoader.loadClass(className);
        return clazz.getDeclaredConstructor().newInstance(args);
      }
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
             | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    // return null;
  }

}
