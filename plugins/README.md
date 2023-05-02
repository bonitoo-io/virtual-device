## Virtual Device Plugins

Two types of plugins can be added to the Virtual Device runner:

   * Item Plugins - for generating primitive values.  Item plugins need to extend the `ItemGenPlugin` class. 
   * Sample Plugins - for generating complete samples including their primitive values. _As of this writing (02.05.2023) this has yet to be implemented_. 

Plugins are jar files with a main class extending `DataGenPlugin`.  The key method from this class that a plugin must implement is `genData()`, which handles the generation of primitives in an ItemPlugin or structures in a SamplePlugin.  

Plugins must also contain a `plugin.props` file that must include at a minimum the following properties. 

   * `plugin.main` - the main class of the plugin extending `ItemGenPlugin` or `SampleGenPlugin` (Samples - To Be Done.).
   * `plugin.name` - a string value for handling the class elsewhere, for example in an Item Configuration.  
   * `plugin.description` - what the plugin does. 
   * `plugin.version` - iteration of the development of the plugin.
   * `plugin.type` - one of `PluginType.Item` or `PluginType.Sample`.
   * `plugin.resultType` - for Item Plugins this can be the primitive type returned by the `genData()` method: `PluginResultType.Double`,`PluginResultType.Long`, `PluginResultType.String`.  For Sample Plugins this can be an object serialized as a `PluginResultType.Json` string.
   * `plugin.label` - default label to be used when serializing the results of an Item Plugin as part of a sample.  

For primitive results of type `Double` a precision property `plugin.dec.prec` of type integer can be added, to specify the floating point decimal precision of a value to be used during serialization.

Additional properties can be defined for use in specific plugins.  

### Plugin Lifecycle

When the default device runner starts it scans the `plugin` directory for any jar files and attempts to load them into the environment.  Subdirectories will not be scanned.  They are therefore an ideal location for storing examples, that can be activated by being copied into the `plugin` directory before starting the runner.  Or the copies can be removed from the directory to remove them from the environment on a subsequent device runner load and run.  On load the plugin class is generated and saved into a registry.  Later new instances can be generated and bound to samples or items based on their configurations.  Plugin instances last until they and the items or samples to which they are bound get garbage collected.  Generally this means for the life of the device runner.  Class references in the registry last for the life of the device runner.  

#### Item plugins

Once an Item Plugin is loaded it can be referenced in an Item configuration.  

```yaml
 - name: "speed"
   label: "speed"
   type: "Plugin"
   pluginName: "AcceleratorPlugin"
   resultType: "Double"
```

Any Item instance created from this configuration will look for the plugin class and its properties by name in the registry.  The factory class `ItemPluginMill` that wraps the registry can then generate a new instance of the ItemPlugin, which then gets assigned as the data generator for that item.    

#### Sample plugins

TBD