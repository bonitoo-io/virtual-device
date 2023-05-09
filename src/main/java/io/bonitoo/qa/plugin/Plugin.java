package io.bonitoo.qa.plugin;

/**
 * The basis for any and all plugins.
 */
public interface Plugin {

  public void onLoad();

  public boolean onEnable();

  public boolean onDisable();
}
