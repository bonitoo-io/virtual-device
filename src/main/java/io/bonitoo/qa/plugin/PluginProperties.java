package io.bonitoo.qa.plugin;

import java.util.Properties;
import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates properties used to configure a data gen plugin.
 */

@Getter
@Setter
public class PluginProperties {

  private static final String DEFAULT_KEY_MAIN = "plugin.main";
  private static final String DEFAULT_KEY_NAME = "plugin.name";
  private static final String DEFAULT_KEY_DESC = "plugin.description";
  private static final String DEFAULT_KEY_VERS = "plugin.version";
  private static final String DEFAULT_KEY_TYPE = "plugin.type";
  private static final String DEFAULT_KEY_RTYP = "plugin.resultType";

  private static final String DEFAULT_KEY_LABEL = "plugin.label";

  private static final String DEFAULT_KEY_PREC = "plugin.decimal.prec";


  // required properties
  private String main;
  private String name;
  private String label;
  private String description;
  private String version;

  private PluginType type;

  private PluginResultType resultType;

  // N.B. can be null
  private Integer prec;

  private Properties properties;

  private static Object fetchRequiredProperty(String key, Properties properties)
      throws PluginConfigException {
    if (properties == null) {
      throw new PluginConfigException("Properties is null");
    }
    Object obj = properties.getProperty(key);
    if (obj == null) {
      throw new PluginConfigException(String.format("Required property %s is not defined", key));
    }
    return obj;
  }

  /**
   * Instantiates a new pluginProperties using all required fields.
   *
   * <p>If the required properties are not included in the properties instance,
   * they will be added.</p>
   *
   * @param main - name of the main class of the plugin.
   * @param name - name of the plugin, used locating it in factories and registries.
   * @param description - what does this plugin do.
   * @param version - latest version.
   * @param type - is it an Item plugin or a Sample plugin.
   * @param resultType - type returned by the required method <code>genData()</code>
   * @param properties - additional properties used by the plugin.
   */
  public PluginProperties(String main,
                          String name,
                          String label,
                          String description,
                          String version,
                          PluginType type,
                          PluginResultType resultType,
                          Properties properties) {
    this.main = main;
    this.name = name;
    this.label = label;
    this.description = description;
    this.version = version;
    this.type = type;
    this.resultType = resultType;
    this.properties = properties;

    if (this.properties.getProperty(DEFAULT_KEY_MAIN) == null) {
      this.properties.setProperty(DEFAULT_KEY_MAIN, this.main);
    }

    if (this.properties.getProperty(DEFAULT_KEY_NAME) == null) {
      this.properties.setProperty(DEFAULT_KEY_NAME, this.name);
    }

    if (this.properties.getProperty(DEFAULT_KEY_DESC) == null) {
      this.properties.setProperty(DEFAULT_KEY_DESC, this.description);
    }

    if (this.properties.getProperty(DEFAULT_KEY_VERS) == null) {
      this.properties.setProperty(DEFAULT_KEY_VERS, this.version);
    }

    if (this.properties.getProperty(DEFAULT_KEY_TYPE) == null) {
      this.properties.setProperty(DEFAULT_KEY_TYPE, String.valueOf(this.type));
    }

    if (this.properties.getProperty(DEFAULT_KEY_RTYP) == null) {
      this.properties.setProperty(DEFAULT_KEY_RTYP, String.valueOf(this.resultType));
    }

    if (this.properties.getProperty(DEFAULT_KEY_LABEL) == null) {
      this.properties.setProperty(DEFAULT_KEY_LABEL, String.valueOf(this.label));
    }

    if (this.properties.getProperty(DEFAULT_KEY_PREC) != null) {
      this.prec = Integer.parseInt(this.properties.getProperty(DEFAULT_KEY_PREC));
    }


  }

  /**
   * Instantiates a pluginProperties object based on java.util.Properties.
   *
   * <p>The <code>properties</code> object must contain all properties required
   * to complete a basic plugin configuration.  Additional properties can be added,
   * which should then be handled by the data generation plugin implementation.</p>
   *
   * <ul>
   *   <li>{@value #DEFAULT_KEY_MAIN} - the main class</li>
   *   <li>{@value #DEFAULT_KEY_NAME} - the name of the plugin</li>
   *   <li>{@value #DEFAULT_KEY_DESC} - a description of the plugin</li>
   *   <li>{@value #DEFAULT_KEY_TYPE} - the type of plugin defined, e.g. Item or Sample</li>
   *   <li>{@value #DEFAULT_KEY_VERS} - a version for the plugin</li>
   *   <li>{@value #DEFAULT_KEY_RTYP} - the return type of this plugin.</li>
   * </ul>
   *
   * @param properties - a java.util.Properties set of properties.
   * @throws PluginConfigException - thrown if a required property is missing.
   */
  public PluginProperties(Properties properties) throws PluginConfigException {
    this.properties = properties;
    this.main = (String) fetchRequiredProperty(DEFAULT_KEY_MAIN, properties);
    this.name = (String) fetchRequiredProperty(DEFAULT_KEY_NAME, properties);
    this.label = (String) fetchRequiredProperty(DEFAULT_KEY_LABEL, properties);
    this.description = (String) fetchRequiredProperty(DEFAULT_KEY_DESC, properties);
    this.version = (String) fetchRequiredProperty(DEFAULT_KEY_VERS, properties);
    this.type = PluginType.valueOf((String) fetchRequiredProperty(DEFAULT_KEY_TYPE, properties));
    this.resultType = PluginResultType.valueOf(
      (String) fetchRequiredProperty(DEFAULT_KEY_RTYP, properties));
    if (properties.containsKey(DEFAULT_KEY_PREC)) {
      this.prec = Integer.parseInt(properties.getProperty(DEFAULT_KEY_PREC));
    }
  }
}
