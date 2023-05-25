## Virtual Device Plugins

Two types of plugins can be added to the Virtual Device runner:

   * Item Plugins - for generating primitive values.  
   * Sample Plugins - for generating complete sample payloads including their primitive item values.  

Plugins are jar files with a main class extending one of two classes:
   * `ItemGenPlugin` for Item plugins. The key method from this class that a plugin must override is:
     * `genData()` - handles the generation of primitive values.
   * `SamplePlugin` for Sample plugins.  Extended classes will need to override or implement the following methods.
      * `<? extends SamplePlugin> create(<? extends SamplePluginConf> conf)` - a static factory method used to generate a plugin instance. 
      * `upate()` - called to update the sample.
      * `toJson()` - serializes the sample contents to the JSON payload. 
      * `onLoad()` - to initialize a new instance of the sample, e.g. for setting special fields or reading additional files.
      * `applyProps()` - to apply any special properties from plugin properties. 

Plugins must also contain a `plugin.props` file.  At a minimum this file needs to define the following properties: 

   * `plugin.main` - the name of the main class extending `ItemGenPlugin` or `SamplePlugin`.
   * `plugin.name` - a string value for working with the class elsewhere, for example in an Item Configuration.  
   * `plugin.description` - what the plugin does. 
   * `plugin.version` - developmental iteration of the plugin.
   * `plugin.type` - one of `Item` or `Sample`, needed by `PluginLoader`.
   * `plugin.resultType` - for Sample Plugins this can be an object serialized as a `Json` string.  For Item Plugins this can be the primitive type returned by the `genData()` method: 
      * `Double`,
      * `Long`, 
      * `String`.  
   * `plugin.label` - default label to be used when serializing the results of an Item Plugin as part of a sample.  This value can be overridden on YAML configuration files. 

For primitive results of type `Double` a precision property `plugin.dec.prec` of type integer can be added, to specify the floating point decimal precision of a value to be used during serialization. (Note see issue [6](https://github.com/bonitoo-io/virtual-device/issues/6)).

Additional properties can be defined for use in specific plugins.  

### Plugin Lifecycle

When the default device runner starts it scans the `plugins/` directory for any jar files and attempts to load them into the environment.  Subdirectories will not be scanned.  They are therefore an ideal location for storing examples, that can be activated by being copied into the `plugins/` directory before starting the runner.  The copies can also be removed from the directory to remove them from the environment on a subsequent device runner load and run.  

When first loaded the plugin class is generated and saved into a registry.  Later, new instances get generated and bound to samples or items based on their YAML configurations.  During the remainder of runtime the plugin needs to generate one of the following.  
   * ItemPlugin - primitive data to be used in sample payload fields.  This is done through the overloaded `genData()` method.  It must generate and return a value of the type specified by the property `plugin.resultType`.
   * SamplePlugin - a complete JSON sample payload.  This is done through the overloaded `update()` method.

These methods will be repeatedly called based on the `interval` property of the device to which they are attached. 

Plugin instances last until they and the items or samples to which they are bound get garbage collected.  Generally this means for the life of the device runner.  Class references in the registry last for the life of the device runner.  

### Item plugins

Once an Item Plugin is loaded it can be referenced in an Item configuration.  

```yaml
 - name: "speed"
   label: "speed"
   type: "Plugin"
   pluginName: "AcceleratorPlugin"
   resultType: "Double"
```

Any Item instance created from this configuration will look for the plugin class and its properties by `pluginName` in the registry.  In this case "AcceleratorPlugin".  The value of `pluginName` must match the property `plugin.name` from the `plugin.props` file.  The factory class `ItemPluginMill` wraps a registry and can generate a new instance of the ItemPlugin.  This instance then gets assigned as the data generator for that item instance. 

*ItemGenPlugin*

The abstract class `ItemGenPlugin`, which extends `DataGenPlugin`, provides the basis for any ItemPlugin to be loaded into the Virtual Device runtime.  Two abstract methods need to be implemented. 

   * `onLoad()` -  intended to be used to set up any background or global values needed by a plugin instance.  For example, it is a good place to set the `enabled` property to `true`.
   * `genData()` - returns an object and accepts a variable array of object arguments.  The return value needs to be of the type defined in `plugin.resultType`.

**An Example Item Plugin**

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
_A configuration using the plugin_
```yaml
    - name: "speed"
      label: "speed"
      type: "Plugin"
      pluginName: "AcceleratorPlugin"
      resultType: "Double"
```

### Sample plugins

A Sample Plugin requires the extension of the following class:

  * `SamplePlugin` - e.g. `InfluxLpSamplePlugin`.  This handles the core work of updating and generating data.  Extended SamplePlugin main classes should also leverage the class level annotation `@SamplePluginConfigClass`, whose field `conf` should be set to the plugin configuration class (see below).  e.g. `@SamplePluginConfigClass(conf = InfluxLPSamplePluginConf.class)`.

Additionally, it is recommended for more complex Sample Plugins that the following be extended or created.

  * A serializer for the sample plugin extending Jackson `StdSerializer` - e.g. `InfluxLPSampleSerializer`.  This converts data to a JSON payload string and should be leveraged in the `toJson()` method. 
  * A configuration for the plugin extending `SamplePluginConf` - e.g. `InfluxLPSamplePluginConf`.  This adds extra parameters to the configuration, if necessary, and is passed to factory methods, whenever a new instance of the SamplePlugin is needed. 
  * A configuration deserializer extending `SampleConfigDeserializer` - e.g. `InfluxLPSamplePluginConfDeserializer`.  This is necessary to generate the above configuration file from a YAML representation.

Failure to implement these additional classes means that their default `SamplePlugin` equivalents are used and the results may be unpredictable. 

_SamplePlugin extended class main_

The class referred to by the plugin property `plugin.main` needs to override or implement the following methods.  

   * `public static Class<? extends SamplePlugin> create(Class<? extends SamplePluginConfig> conf)` - this factory style method works like a callback in `SamplePluginMill` to generate new instances of the plugin.
   * `public Sample update()` - called by the device to update the internal state of the plugin, in other words to advance a counter or to generate new values for fields.
   * `public void onLoad()` - needed to set up the values for the initial state of the plugin instance.  For example, set `enabled=true`.
   * `public void applyProps(PluginProperties props)` - needed to set any special values defined in the `plugin.props` file. 
   * `public String toJson()` - needed to serialize the current state of the plugin to a JSON payload. 

**An Example Sample Plugin**

_SamplePlugin example_
```java
@Getter
@SamplePluginConfigClass(conf = LPFileReaderPluginConf.class)
@JsonSerialize(using = LPFileReaderPluginSerializer.class)
public class LPFileReaderPlugin extends SamplePlugin {

     String lpFile;

     List<LineProtocol> lines;

     int index;
     public static LPFileReaderPlugin create(LPFileReaderPluginConf conf){

          // should have been loaded into SamplePluginMill by PluginLoader
          return new LPFileReaderPlugin(SamplePluginMill.getPluginProps(conf.getPlugin()), conf);

     }

     public LPFileReaderPlugin(PluginProperties props, LPFileReaderPluginConf conf) {
          super(props, conf);
          lines = new ArrayList<>();
          lpFile = conf.getSource() != null ? conf.getSource() : (String) props.getProperties().get("default.lp.file");
          index = 0;
     }

     @Override
     public LPFileReaderPlugin update(){
          index++;
          if(index >= lines.size()){
               index = 0;
          }
          lines.get(index).setTimestamp(System.currentTimeMillis());
          return this;
     }

     @Override
     public String toJson() throws JsonProcessingException {
          ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
          return ow.writeValueAsString(this);
     }

     protected File resolveSourceFile() throws URISyntaxException {
          ClassLoader loader = Thread.currentThread().getContextClassLoader();
          URL fileUrl = loader.getResource(lpFile);
          File result;
          if( fileUrl != null){ // located as resource
               return new File(Objects.requireNonNull(loader.getResource(lpFile)).toURI());
          }

          return new File(lpFile);
     }

     @Override
     public void onLoad(){
          super.onLoad();

          try {
               File inputFile = resolveSourceFile();
               try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                    String line;
                    while((line = br.readLine()) != null){
                         LineProtocol lp = LineProtocol.parseLP(line);
                         if(lp != null) {
                              lines.add(LineProtocol.parseLP(line));
                         }
                    }
               } catch (IOException e) {
                    throw new LPFileReaderPluginException(e);
               }
          } catch (URISyntaxException e) {
               throw new LPFileReaderPluginException(e);
          }

     }

     @Override
     public void applyProps(PluginProperties pluginProperties) {
          // holder
     }

     public LineProtocol get(int ndx){
          return lines.get(ndx);
     }

     public LineProtocol getCurrent(){
          return get(this.index);
     }

}
```

_plugin.props for the above_

```properties
plugin.main=io.bonitoo.virdev.plugin.LPFileReaderPlugin
plugin.name=LPFileReader
plugin.description=Reads Line Protocol files and sends to MQTT
plugin.version=0.1
plugin.type=Sample
plugin.resultType=Json
plugin.label=lp
default.lp.file=test.lp
```
_A Sample configuration using the above_
```yaml
  - id: "random"
    name: "LPFileReaderConf"
    topic: "test/linep"
    items: [ ]
    plugin: "LPFileReader"
    source: "./plugins/examples/lpFileReader/data/myTestLP.lp"
```

