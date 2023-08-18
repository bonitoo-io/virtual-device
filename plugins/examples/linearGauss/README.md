## LinearGauss Data Generator

The LinearGauss Data Generator leverages the `gaussNormalFilter()` filter function of the default built in `NumGenerator`.  The default generator uses this method when generating values along a sinusoidal curve.  This plugin does away with the sinusoidal aspect of the generator and simply returns values between `min` and `max` based on a standard gaussian distribution.

The source code is located in the [_virtual-device-plugins_ repository](https://github.com/bonitoo-io/virtual-device-plugins/tree/main/examples/linearGauss).

To use.
  * Copy the plugin jar to the root `plugins` directory. 
  * Add the plugin item to the YAML configuration, as shown in `sampleRunnerConfig.yml`

Example configuration:

```yaml
...
        items:
          - name: "linearGaussConf"
            label: "lgVal"
            type: "Plugin"
            pluginName: "LinearGaussGen"
            resultType: "Double"
            min: -6.0
            max: 55.0
...
```