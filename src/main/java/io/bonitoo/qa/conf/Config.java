package io.bonitoo.qa.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.util.LogHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the basic configuration.
 */
public class Config {

  static final String envConfigFile = "VIRTUAL_DEVICE_CONFIG";
  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static RunnerConfig runnerConfig;
  static Properties props;

  static String configFile = System.getenv(envConfigFile) == null ? "virtualdevice.props" :
      System.getenv(envConfigFile).trim();

  private static void readProps() {
    props = new Properties();
    logger.info(LogHelper.buildMsg("config", "Reading base config", configFile));
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try (
        InputStream is = loader.getResourceAsStream(configFile) == null
            ? Files.newInputStream(new File(configFile).toPath()) :
            loader.getResourceAsStream(configFile); ) {
      props.load(is);
    } catch (IOException e) {
      logger.error(LogHelper.buildMsg("config", "Load failure",
          String.format("Unable to load config file %s", configFile)));
      logger.error(LogHelper.buildMsg("config", "Load exception", e.toString()));
      System.exit(1);
    }

    // Overwrite properties set in JVM with system values
    for (String key : props.stringPropertyNames()) {
      if (System.getProperty(key) != null) {
        props.setProperty(key, System.getProperty(key));
      }
    }
  }

  protected static void readRunnerConfig() {
    if (props == null) {
      readProps();
    }
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    logger.info(LogHelper.buildMsg("config", "Reading runner config",
        props.getProperty("runner.conf")));

    InputStream runnerConfStream = loader.getResourceAsStream(props.getProperty("runner.conf"));

    try {

      if (runnerConfStream == null) {
        runnerConfStream = Files.newInputStream(
          new File(props.getProperty("runner.conf")
          ).toPath());
      }

      ObjectMapper om = new ObjectMapper(new YAMLFactory());
      runnerConfig = om.readValue(runnerConfStream, RunnerConfig.class);
      if (runnerConfig == null) {
        throw new VDevConfigException(
          String.format("Failed to parse config file %s", props.getProperty("runner.conf"))
        );
      }
      logger.info(LogHelper.buildMsg("config",
          "Parse runner config success",
          props.getProperty("runner.conf")));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to get a property by key.
   *
   * @param key - the key of the desired property.
   *
   * @return - the value of the desired property.
   */
  public static String getProp(String key) {
    if (props == null) {
      readProps();
    }
    return props.getProperty(key);
  }

  /**
   * Gets the properties.
   *
   * @return - the top level properties.
   */
  public static Properties getProps() {
    if (props == null) {
      readProps();
    }
    return props;
  }

  /**
   * Gets the broker configuration.
   *
   * @return - a broker configuration.
   */
  public static BrokerConfig getBrokerConf() {
    if (runnerConfig == null) {
      readRunnerConfig();
    }
    return runnerConfig.getBroker();
  }

  /**
   * Gets a list of sample configs for the specified device.
   *
   * @param ofDev - index of the device.
   *
   * @return - list of sample configs. 
   */
  public static List<SampleConfig> getSampleConfs(int ofDev) {
    if (runnerConfig == null) {
      readRunnerConfig();
    }
    return runnerConfig.getDevices().get(ofDev).getSamples();
  }

  /**
   * Returns the device configs in the runner config.
   *
   * @return - list of device configs.
   */
  public static List<DeviceConfig> getDeviceConfs() {
    if (runnerConfig == null) {
      readRunnerConfig();
    }
    return runnerConfig.getDevices();
  }

  /**
   * Gets the whole inner runner config.
   *
   * @return - the runner config.
   */
  public static RunnerConfig getRunnerConfig() {
    if (runnerConfig == null) {
      readRunnerConfig();
    }
    return runnerConfig;
  }

  public static Long ttl() {
    return runnerConfig.getTtl();
  }

  public static DeviceConfig deviceConf(int i) {
    return getDeviceConfs().get(i);
  }

  public static SampleConfig sampleConf(int ofDev, int i) {
    return getSampleConfs(ofDev).get(i);
  }

  //alias for above
  public static BrokerConfig brokerConf() {
    return getBrokerConf();
  }

  /**
   * Resets the entire configuration.  Used mainly in testing.
   */
  public static void reset() {
    runnerConfig = null;
    ItemConfigRegistry.clear();
    SampleConfigRegistry.clear();
    runnerConfig = getRunnerConfig();
  }
}
