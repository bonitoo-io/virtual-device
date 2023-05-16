package io.bonitoo.qa.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class PluginTest {

  public static enum PluginState {
    Zero,
    Loaded,
    Enabled,
    Disabled,
    Instantiated
  }

  private static PluginState state = PluginState.Zero;

  @BeforeEach
  public void zeroState(){
    state = PluginState.Zero;
  }

  public static class SimpleItemPlugin implements Plugin {

    boolean enabled = false;

    @Override
    public void onLoad() {
      state = PluginState.Loaded;
    }

    @Override
    public boolean onEnable() {
      if(state != PluginState.Loaded){
        return false;
      }
      state = PluginState.Enabled;
      enabled = true;
      return true;
    }

    @Override
    public boolean onDisable() {
      state = PluginState.Disabled;
      return enabled = false;
    }

    @Override
    public void applyProps(PluginProperties props) {
      if(state == PluginState.Enabled) {
        state = PluginState.Instantiated;
      }
    }
  }

  @Test
  public void loadPlugin(){
     SimpleItemPlugin plugin = new SimpleItemPlugin();
     assertEquals(PluginState.Zero,state);
     plugin.onLoad();
     assertEquals(PluginState.Loaded,state);
  }

  @Test
  public void enablePlugin(){
    SimpleItemPlugin plugin = new SimpleItemPlugin();
    assertEquals(PluginState.Zero,state);
    assertFalse(plugin.onEnable());
    plugin.onLoad();
    assertTrue(plugin.onEnable());
    assertEquals(PluginState.Enabled,state);
  }

  @Test
  public void disablePlugin(){
    SimpleItemPlugin plugin = new SimpleItemPlugin();
    assertEquals(PluginState.Zero,state);
    assertFalse(plugin.onDisable());
    assertEquals(PluginState.Disabled,state);
  }

  @Test void applyPluginProps(){
    PluginProperties props = new PluginProperties(
      SimpleItemPlugin.class.getName(),
      "SimpleTest",
      "test",
      "A test plugin",
      "0.0.1",
      PluginType.Item,
      PluginResultType.String,
      new Properties()
      );

    SimpleItemPlugin plugin = new SimpleItemPlugin();
    assertEquals(PluginState.Zero,state);
    plugin.onLoad();
    plugin.applyProps(props);
    assertEquals(PluginState.Loaded, state);
    plugin.onEnable();
    plugin.applyProps(props);
    assertEquals(PluginState.Instantiated, state);

  }

}
