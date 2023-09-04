package io.bonitoo.qa.plugin.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.data.Sample;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.PluginResultType;
import io.bonitoo.qa.plugin.PluginType;
import io.bonitoo.qa.plugin.sample.SamplePlugin;
import io.bonitoo.qa.plugin.sample.SamplePluginConfig;
import io.bonitoo.qa.plugin.sample.SamplePluginConfigClass;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Properties;


import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class SamplePluginTest {

  protected static PluginProperties defaultProps = new PluginProperties(
    FooSamplePlugin.class.getName(),
    "FooTestPlugin",
    "msg",
    "A test sample plugin",
    "0.1",
    PluginType.Sample,
    PluginResultType.Json,
    new Properties(){{ put("some.val", "PropertyVal"); }}
  );

  protected static SamplePluginConfig conf = new SamplePluginConfig("ASDF-1234",
    "FooPluginConfig",
    "test/foo",
    new ArrayList<>(),
    defaultProps.getName());


  public static class FooSamplePlugin extends SamplePlugin {

    @JsonSerialize
    String value = "unset";

    @JsonSerialize
    int count = 0;

    public FooSamplePlugin(PluginProperties props, SamplePluginConfig config) {
      super(props, config);
    }

    public FooSamplePlugin(PluginProperties props, SamplePluginConfig config, Object[] args){
      super(props, config);
      this.value = (String) args[0];
      this.count = (Integer) args[1];
    }

    public static FooSamplePlugin create(SamplePluginConfig config){
      FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, config);
      fsp.applyProps(defaultProps);
      return fsp;
    }

    @Override
    public Sample update() {
      super.update();
      if(enabled) {
        count++;
      }
      return this;
    }

    @Override
    public String toJson() throws JsonProcessingException {
      if(this.enabled) {
        ObjectWriter ow = new ObjectMapper().writer();
        return ow.writeValueAsString(this);
      }else{
        return "{}";
      }
    }

    @Override
    public void onLoad() {
      enabled = true;
      value = "fooLoaded";
    }

    @Override
    public void applyProps(PluginProperties props) {
      value = (String)props.getProperties().get("some.val");
    }
  }

  @Test
  public void createBasicSamplePlugin(){

    FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, conf);

    assertEquals(0, fsp.count);
    assertEquals("unset", fsp.value);
    assertEquals("ASDF-1234", fsp.getId());
    assertEquals("test/foo", fsp.getTopic());
    assertEquals("FooTestPlugin", fsp.getName());
    assertEquals(FooSamplePlugin.class.getName(), fsp.getMain());
    assertEquals("A test sample plugin", fsp.getDescription());
    assertEquals("PropertyVal", fsp.getProperties().get("some.val"));

  }


  //Using function
  @Test
  public void createSamplePuginFactoryFunction(){

    FooSamplePlugin fsp = (FooSamplePlugin) SamplePlugin.of(
      FooSamplePlugin::create,
      conf,
      defaultProps
    );

    fsp.onEnable();
    fsp.update();

    assertEquals(defaultProps.getProperties().get("some.val"), fsp.value);
    assertEquals(1, fsp.count);

  }

  //Using method
  @Test
  public void createSamplePluginFactoryReflection() throws NoSuchMethodException {

    FooSamplePlugin fsp = (FooSamplePlugin) SamplePlugin.of(
      FooSamplePlugin.class.getDeclaredMethod("create", SamplePluginConfig.class),
      conf,
      defaultProps
    );

    fsp.onEnable();
    fsp.update();

    assertEquals(defaultProps.getProperties().get("some.val"), fsp.value);
    assertEquals(1, fsp.count);

  }

  @Test
  public void loadPlugin(){

    FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, conf);
    assertEquals("unset", fsp.value);
    assertFalse(fsp.enabled);
    fsp.onLoad();
    assertEquals("fooLoaded", fsp.value);
    assertTrue(fsp.enabled);

  }

  @Test
  public void applyPluginProps(){
    FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, conf);
    assertEquals("unset", fsp.value);
    assertFalse(fsp.enabled);
    fsp.applyProps(defaultProps);
    assertEquals("PropertyVal", fsp.value);
    assertFalse(fsp.enabled);
  }

  @Test
  public void pluginUpdate(){
    FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, conf);
    assertEquals("unset", fsp.value);
    assertFalse(fsp.enabled);
    assertEquals(0, fsp.count);
    fsp.update();
    assertEquals(0, fsp.count);
    fsp.onEnable();
    fsp.update();
    assertEquals(1, fsp.count);
  }

  @Test
  public void pluginToJson() throws JsonProcessingException {
    FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, conf);
    assertEquals("unset", fsp.value);
    assertFalse(fsp.enabled);
    assertEquals("{}", fsp.toJson());
    fsp.onLoad();
    fsp.applyProps(defaultProps);
    fsp.onEnable();
    fsp
      .update()
      .update();

    System.out.println("DEBUG fsp.toJson() " + fsp.toJson());

    assertTrue(
      fsp.toJson().matches("\\{\"id\":\"ASDF-1234\",\"timestamp\":[0-9]*,\"value\":\"PropertyVal\",\"count\":2\\}")
    );
  }

  @Test
  public void enableDisableTest(){
    FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, conf);
    assertFalse(fsp.enabled);
    fsp.update();
    assertEquals(0, fsp.count);
    fsp.onEnable();
    assertTrue(fsp.enabled);
    fsp.update();
    assertEquals(1, fsp.count);
    fsp.onDisable();
    assertFalse(fsp.enabled);
    fsp.update();
    assertEquals(1, fsp.count);
  }

  @Test
  public void propHelpers(){
    FooSamplePlugin fsp = new FooSamplePlugin(defaultProps, conf);
    assertEquals(defaultProps.getName(), fsp.getName());
    assertEquals(defaultProps.getMain(), fsp.getMain());
    assertEquals(defaultProps.getDescription(), fsp.getDescription());
    assertEquals(defaultProps.getProperties(), fsp.getProperties());
    assertEquals(defaultProps.getProperties().get("some.val"), fsp.getProp("some.val"));
  }

  static class EmptySamplePluginConf extends SamplePluginConfig {

    public EmptySamplePluginConf() {
      super();
      this.setItems(new ArrayList<>());
    }
  }

  @SamplePluginConfigClass( conf = EmptySamplePluginConf.class)
  static class EmptySamplePlugin extends SamplePlugin {

    /**
     * Constructs a Sample Plugin.
     *
     * @param props  - plugin properties.
     * @param config - configuration for the generated sample.
     */
    public EmptySamplePlugin(PluginProperties props, SampleConfig config) {
      super(props, config);
    }

    @Override
    public void applyProps(PluginProperties props) {
      //holder
    }

    @Override
    public String toJson() throws JsonProcessingException {
      return null;
    }
  }

  @Test
  public void samplePluginConfAnnotationTest(){

    PluginProperties emProps = new PluginProperties(
      EmptySamplePlugin.class.getName(),
      "EmptyTestPlugin",
      "msg",
      "A test sample plugin",
      "0.1",
      PluginType.Sample,
      PluginResultType.Json,
      new Properties(){{ put("some.val", "PropertyVal"); }}
    );

    Annotation[] annotations = EmptySamplePlugin.class.getAnnotations();

    assertEquals(1, annotations.length);
    assertEquals(EmptySamplePluginConf.class.getName(),
      ((SamplePluginConfigClass) annotations[0]).conf().getName());

  }

}
