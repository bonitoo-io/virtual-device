package io.bonitoo.qa.plugin;

public interface Plugin {

  public void onLoad();

  public boolean onEnable();

  public boolean onDisable();
}
