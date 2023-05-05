package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.VirDevConfigException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class ItemPluginMillTest {

  @Test
  public void unknownKeyThrowsException(){
    assertThrowsExactly(VirDevConfigException.class,
      () -> ItemPluginMill.getPluginProps("FooBar"),
      "Plugin key: FooBar unknown.  Is it in the plugins/ directory?");
  }
}
