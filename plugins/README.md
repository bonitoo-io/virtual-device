## Virtual Device Plugins

Two types of plugins can be added to the Virtual Device runner:

   * Item Plugins - for generating primitive values.  Item plugins need to extend the `ItemGenPlugin` class. 
   * Sample Plugins - for generating complete samples including their primitive item values. _As of this writing (02.05.2023) this has yet to be implemented_. 

Plugins are jar files with a main class extending `DataGenPlugin`.  The key method from this class that a plugin must implement is `genData()`, which handles the generation of primitives in an ItemPlugin or structures in a SamplePlugin.  

Plugins must also contain a `plugin.props` file.  At a minimum this file needs to define the following properties: 

   * `plugin.main` - the main class of the plugin extending `ItemGenPlugin` or `SampleGenPlugin` (_Samples - To Be Done._).
   * `plugin.name` - a string value for working with the class elsewhere, for example in an Item Configuration.  
   * `plugin.description` - what the plugin does. 
   * `plugin.version` - developmental iteration of the plugin.
   * `plugin.type` - one of `Item` or `Sample`.
   * `plugin.resultType` - for Sample Plugins this can be an object serialized as a `Json` string.  For Item Plugins this can be the primitive type returned by the `genData()` method: 
      * `Double`,
      * `Long`, 
      * `String`.  
   * `plugin.label` - default label to be used when serializing the results of an Item Plugin as part of a sample.  

For primitive results of type `Double` a precision property `plugin.dec.prec` of type integer can be added, to specify the floating point decimal precision of a value to be used during serialization.

Additional properties can be defined for use in specific plugins.  

### Plugin Lifecycle

When the default device runner starts it scans the `plugins/` directory for any jar files and attempts to load them into the environment.  Subdirectories will not be scanned.  They are therefore an ideal location for storing examples, that can be activated by being copied into the `plugins/` directory before starting the runner.  The copies can also be removed from the directory to remove them from the environment on a subsequent device runner load and run.  

On load the plugin class is generated and saved into a registry.  Later, new instances get generated and bound to samples or items based on their configurations.  Based on the interval pulse of a device containing a sample with an item to which a plugin instance is bound, the plugins `genData()` method gets called.  It must generate and return a value of the type specified by the property `plugin.resultType`.  Plugin instances last until they and the items or samples to which they are bound get garbage collected.  Generally this means for the life of the device runner.  Class references in the registry last for the life of the device runner.  

#### Item plugins

Once an Item Plugin is loaded it can be referenced in an Item configuration.  

```yaml
 - name: "speed"
   label: "speed"
   type: "Plugin"
   pluginName: "AcceleratorPlugin"
   resultType: "Double"
```

Any Item instance created from this configuration will look for the plugin class and its properties by `pluginName` in the registry.  In this case "AcceleratorPlugin".  The factory class `ItemPluginMill` that wraps the registry can then generate a new instance of the ItemPlugin.  This then gets assigned as the data generator for that item instance. 

**ItemGenPlugin**

The abstract class `ItemGenPlugin`, which extends `DataGenPlugin`, provides the basis for any ItemPlugin to be loaded into the Virtual Device runtime.  Two abstract methods need to be implemented. 

   * `onLoad()` -  intended to be used to set up any background or global values needed by the plugin.  For example, it is a good place to set the `enabled` property to `true`.
   * `genData()` - returns an object and accepts a variable array of object arguments.  The return value needs to be of the type defined in `plugin.resultType`.

**An Example Plugin**

_AcceleratorPlugin.java_
```java
package io.bonitoo.virdev.plugin;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.plugin.ItemGenPlugin;
import io.bonitoo.qa.plugin.PluginProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class AcceleratorPlugin extends ItemGenPlugin {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static double INITIAL_SPEED = 1.0;
  private static double INITIAL_ACCEL = 1.0;
  private static double SPEED_LIMIT = 60.0;

  double speed; // m/s
  double accel; // m/s**

  long lastRecordStamp;

  public AcceleratorPlugin(){
    this.speed = INITIAL_SPEED;
    this.accel = INITIAL_ACCEL;
    this.lastRecordStamp = System.currentTimeMillis();
  }

  @Override
  public Double getCurrentVal() {
    return speed;
  }

  public AcceleratorPlugin(PluginProperties props, ItemConfig config, boolean enabled, double speed, double accel) {
    super(props, config, enabled);
    this.speed = speed;
    this.accel = accel;
    this.lastRecordStamp = System.currentTimeMillis();
  }

  @Override
  public void onLoad() {

    this.speed = props.getProperties().getProperty("initial.speed") == null ?
      INITIAL_SPEED : Double.parseDouble(props.getProperties().getProperty("initial.speed"));
    this.accel = props.getProperties().getProperty("initial.accel") == null ?
      INITIAL_ACCEL : Double.parseDouble(props.getProperties().getProperty("initial.accel"));

    this.enabled = true;
  }

  @Override
  public Object genData(Object... objects) {
    accel = changeAccel(accel, speed);
    long currTimeStamp = System.currentTimeMillis();
    double timeFactor = currTimeStamp - lastRecordStamp;
    lastRecordStamp = currTimeStamp;
    speed += (accel * (timeFactor/1000));
    logger.info(String.format("speed: %.5f", speed));
    return speed;
  }

  private double changeAccel(double curAccel, double curSpeed){
    double delta = Math.random() * 2;
    if(curSpeed < 0){
      if(curSpeed < SPEED_LIMIT /-3){
        return curAccel + delta;
      }else{
        return curAccel - delta;
      }
    }else{
      if(curSpeed > SPEED_LIMIT /3){
        return curAccel - delta;
      }else{
        return curAccel + delta;
      }
    }
  }
}
```
_plugin.props for the above_

```properties
plugin.main=io.bonitoo.virdev.plugin.AcceleratorPlugin
plugin.name=AcceleratorPlugin
plugin.description=A toy plugin for oscillating speed values
plugin.version=0.1
plugin.type=Item
plugin.resultType=Double
plugin.label=speed
initial.speed=3.0
initial.accel=0.5
```

#### Sample plugins

A Sample Plugin requires the extension and implementation of four basic classes. 

  * The sample plugin itself - extends `SamplePlugin` - e.g. `InfluxLpSamplePlugin`.  This handles the core work of updating and generating data. 
  * The serializer for the sample plugin - extends a Jackson `StdSerializer` - e.g. `InfluxLPSampleSerializer`.  This converts that data to a JSON string. 
  * The configuration for the plugin - extends `SamplePluginConf` - e.g. `InfluxLPSamplePluginConf`.  This adds extra parameters to the configuration, if necessary, and is passed to factory methods, whenever a new instance of the SamplePlugin is needed. 
  * The configuration deserializer - extends `SampleConfigDeserializer` - e.g. `InfluxLPSamplePluginConfDeserializer`.  This is necessary to generate the above configuration file from a YAML representation. 

