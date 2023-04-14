package io.bonitoo.qa.plugin;

import io.bonitoo.qa.data.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

@Getter
@Setter
public class PluginProperties {

  private static final String DEFAULT_KEY_MAIN = "plugin.main";
  private static final String DEFAULT_KEY_NAME = "plugin.name";
  private static final String DEFAULT_KEY_DESC = "plugin.description";
  private static final String DEFAULT_KEY_VERS = "plugin.version";
  private static final String DEFAULT_KEY_TYPE = "plugin.type";
  private static final String DEFAULT_KEY_RTYP = "plugin.resultType";


  // required properties
  private String main;
  private String name;
  private String description;
  private String version;

  private PluginType type;

  private PluginResultType resultType;

  private Properties properties;

  public static Object fetchRequiredProperty(String key, Properties properties) throws PluginConfigException {
    if (properties == null) {
      throw new PluginConfigException("Properties is null");
    }
    Object obj = properties.getProperty(key);
    if (obj == null) {
      throw new PluginConfigException(String.format("Required property %s is not defined"));
    }
    return obj;
  }

  public PluginProperties(String main,
                          String name,
                          String description,
                          String version,
                          PluginType type,
                          PluginResultType resultType,
                          Properties properties) {
    this.main = main;
    this.name = name;
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

  }

  public PluginProperties(Properties properties) throws PluginConfigException {
    this.properties = properties;
    this.main = (String) fetchRequiredProperty(DEFAULT_KEY_MAIN, properties);
    this.name = (String) fetchRequiredProperty("plugin.name", properties);
    this.description = (String) fetchRequiredProperty("plugin.description", properties);
    this.version = (String) fetchRequiredProperty("plugin.version", properties);
    this.type = PluginType.valueOf((String) fetchRequiredProperty("plugin.type", properties));
    this.resultType = PluginResultType.valueOf((String) fetchRequiredProperty("plugin.resultType", properties));
  }
}
