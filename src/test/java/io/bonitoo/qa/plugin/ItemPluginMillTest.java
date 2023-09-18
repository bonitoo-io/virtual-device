package io.bonitoo.qa.plugin;

import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@Tag("unit")
public class ItemPluginMillTest {

  @Test
  public void unknownKeyThrowsException(){
    assertThrowsExactly(VirDevConfigException.class,
      () -> ItemPluginMill.getPluginProps("FooBar"),
      "Plugin key: FooBar unknown.  Is it in the plugins/ directory?");
  }
}
