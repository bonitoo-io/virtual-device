package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for data generators and Item plugins.
 *
 * @param <T> - a data configuration object.
 */
public abstract class DataGenerator<T extends DataConfig> {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public abstract Object genData(Object... args);

  /**
   * Factory method for instantiating data generators.
   *
   * @param className - name of the data generator class to be instantiated.
   * @param args - any additional args to be sent to a constructor.
   * @return - a new data generator instance.
   */
  @SuppressWarnings("unchecked")
  public static DataGenerator<? extends DataConfig> create(String className, Object... args) {
    ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
    try {
      // check if plugin
      if (ItemPluginMill.hasPluginClass(className)) {
        logger.info(String.format("Creating DataGenerator instance of plugin class %s", className));
        return (DataGenerator<? extends DataConfig>) ItemPluginMill.getPluginClassByName(className)
            .getDeclaredConstructor().newInstance(args);
      } else { // try systemLoader
        logger.info(String.format("Creating DataGenerator instance of internal class %s",
            className));
        Class<DataGenerator<? extends DataConfig>> clazz =
            (Class<DataGenerator<? extends DataConfig>>) systemLoader.loadClass(className);
        return clazz.getDeclaredConstructor().newInstance(args);
      }
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
             | InstantiationException | IllegalAccessException e) {
      throw new VirDevConfigException(
          new Throwable(
            String.format("Unable to instantiate data generator class %s", className), e));
    }
  }

}
