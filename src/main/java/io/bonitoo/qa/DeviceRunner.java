package io.bonitoo.qa;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.device.Device;
import io.bonitoo.qa.device.GenericDevice;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.plugin.Plugin;
import io.bonitoo.qa.plugin.PluginConfigException;
import io.bonitoo.qa.plugin.PluginLoader;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core runner for devices.
 */
public class DeviceRunner {
  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Starting point.
   *
   * @param args - standard args.
   * @throws JsonProcessingException - exception thrown on bad configuration.
   */
  public static void main(String[] args) throws JsonProcessingException {

    loadPlugins();

    List<Device> devices = new ArrayList<>();

    List<DeviceConfig> devConfigs = Config.getDeviceConfs();

    BrokerConfig broker = Config.getBrokerConf();

    for (DeviceConfig devConf : devConfigs) {
      for (int i = 0; i < devConf.getCount(); i++) {
        if (devConf.getCount() > 1) {
          DeviceConfig copyDevConfig = new DeviceConfig(devConf, (i + 1));
          devices.add(GenericDevice.numberedDevice(MqttClientBlocking.client(broker,
              copyDevConfig.getId()),
              copyDevConfig, (i + 1)));
        } else {
          devices.add(GenericDevice.singleDevice(MqttClientBlocking.client(broker,
              devConf.getId()),
              devConf));
        }
      }
    }

    logger.debug("ItemConfigRegistry keys " + ItemConfigRegistry.keys());

    ExecutorService service = Executors.newFixedThreadPool(devices.size());

    devices.forEach(service::execute);

    try {
      boolean terminated = service.awaitTermination(Config.ttl(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    service.shutdown();

  }

  /**
   *  General method for loading plugins.
   *
   *  <p>Looks for the property <code>plugins.dir</code> and then
   *  loads ale plugin jar files found within.</p>
   */
  public static void loadPlugins() {

    String pluginsDir = Config.getProp("plugins.dir");
    if (pluginsDir == null) {
      throw new VirDevConfigException("plugins.dir property returns null");
    }

    File[] pluginFiles = new File(pluginsDir).listFiles((dir, name) ->
      name.toLowerCase().endsWith(".jar")
    );

    if (pluginFiles == null || pluginFiles.length == 0) {
      logger.warn(String.format("No plugins found in plugin directory: %s", pluginsDir));
      return;
    }

    logger.info(String.format("Loading plugins from plugin directory: %s", pluginsDir));

    for (File f : pluginFiles) {
      logger.info(String.format("Loading plugin %s", f.getName()));
      try {
        Class<? extends Plugin> clazz = PluginLoader.loadPlugin(f);
        if (clazz != null) {
          logger.info(String.format("loaded plugin %s", clazz.getName()));
        } else {
          logger.warn("Plugin load returned null");
        }
      } catch (IOException | PluginConfigException
               | ClassNotFoundException | NoSuchFieldException
               | IllegalAccessException e) {
        throw new VirDevConfigException(e);
      }
    }

  }

}
