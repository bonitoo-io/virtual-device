package io.bonitoo.qa.plugin;

/**
 * Generalized exception for issues with plugin configuration.
 */
public class PluginConfigException extends Exception {

  public PluginConfigException(String msg) {
    super(msg);
  }

  public PluginConfigException(String msg, Throwable t) {
    super(msg, t);
  }
}