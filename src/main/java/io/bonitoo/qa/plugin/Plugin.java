package io.bonitoo.qa.plugin;

/**
 * The basis for any and all plugins.
 */
public interface Plugin {

  /**
   * Called when plugin is initially loaded.  Intended
   * to initialize Class level state.
   */
  public void onLoad();

  /**
   * Called when plugin is enabled.
   *
   * @return - the enabled value.
   */
  public boolean onEnable();

  /**
   * Called when plugin is disabled.
   *
   * @return - the enabled value.
   */
  public boolean onDisable();

  /**
   * Called when instantiating the plugin.  Intended to
   * use any properties to set an initial state for the new instance.
   *
   * @param props - properties taken from plugin properties file.
   */
  public void applyProps(PluginProperties props);
}
