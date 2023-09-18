package io.bonitoo.qa.plugin.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.device.GenericDevice;
import io.bonitoo.qa.mqtt.client.MqttClientBlocking;
import io.bonitoo.qa.plugin.*;
import io.bonitoo.qa.plugin.eg.*;
import io.bonitoo.qa.plugin.util.JarTool;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("intg")
public class SamplePluginIntegrationTest {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Mock
  MqttClientBlocking mockClient;

  private static final String DEFAULT_VALUE = "Foo";
  private static final File pluginDir = new File("src/test/plugins");
  private static final String testJarName = pluginDir.getPath() + "/samplePluginTest.jar";

  private static final Properties defaultProps = new Properties();

  @BeforeAll
  public static void setup() throws IOException {

    defaultProps.load(SamplePluginIntegrationTest.class.getClassLoader()
      .getResourceAsStream("influxLPSamplePlugin.props"));

    if(!pluginDir.exists()){
      if(!pluginDir.mkdirs()){
        throw new RuntimeException("Failed to make dir " + pluginDir);
      };
    }

    JarTool jTool = new JarTool();
    jTool.startManifest();
    jTool.addToManifest("Main-Class", InfluxLPSamplePlugin.class.getName());
    JarOutputStream target = jTool.openJar(testJarName);

    logger.debug(String.format("Path replacements %s.class", InfluxLPSamplePlugin.class.getName().replace(".", "/")));

    jTool.addFile(target, System.getProperty("user.dir")
        + "/target/test-classes",
      System.getProperty("user.dir")
        + "/target/test-classes/" + InfluxLPSamplePlugin.class.getName().replace(".", "/") + ".class");

    jTool.addFile(target, System.getProperty("user.dir")
        + "/target/test-classes",
      System.getProperty("user.dir")
        + "/target/test-classes/" + InfluxLPSampleSerializer.class.getName().replace(".", "/") + ".class");

    jTool.addFile(target, System.getProperty("user.dir")
        + "/target/test-classes",
      System.getProperty("user.dir")
        + "/target/test-classes/" + InfluxLPSamplePluginConf.class.getName().replace(".", "/") + ".class");

    jTool.addFile(target, System.getProperty("user.dir")
        + "/target/test-classes",
      System.getProperty("user.dir")
        + "/target/test-classes/" + InfluxLPSamplePluginConfDeserializer.class.getName().replace(".", "/") + ".class");

    jTool.addRenamedFile(target, System.getProperty("user.dir")
        + "/src/test/resources",
      System.getProperty("user.dir")
        + "/src/test/resources/influxLPSamplePlugin.props",
      "plugin.props"
    );

    target.close();

  }

  @BeforeEach
  public void zeroMill() throws InterruptedException {
    reset(mockClient);
    lenient().when(mockClient.connect()).thenReturn(mockClient);
    SamplePluginMill.clear();
  }

  @AfterEach
  public void clearMill(){
    SamplePluginMill.clear();
  }

  @AfterAll
  public static void cleanup(){
    File pluginFile = new File(testJarName);
    pluginFile.deleteOnExit();
    pluginDir.deleteOnExit();
  }

  @Getter
  static class Tags {
    String foo;
    String bar;
  }

  @Getter
  static class Fields {
    double temperature;
    double pressure;
  }

  @Getter
  static class InfluxLPTestSample {
    String measurement;
    Tags tags;
    Fields fields;
    String timestamp;

  }

  @Test
  public void loadSamplePlugin()
    throws IOException, PluginConfigException, InvocationTargetException,
    NoSuchMethodException, InstantiationException, IllegalAccessException {

    assertEquals(0, SamplePluginMill.size());

    ItemNumConfig iConfigTemp = new ItemNumConfig("temp", "temperature", ItemType.Double, 10, 40, 1, 0.1, 4);
    ItemNumConfig iConfigPress = new ItemNumConfig("press", "pressure", ItemType.Double, 26, 32, 1, 0.1, 4);

    File pluginFile = new File(testJarName);

    try {
      Class<? extends Plugin> clazz = PluginLoader.loadPlugin(pluginFile);
      assertEquals(clazz, SamplePluginMill.getPluginClass("InfluxLPSamplePlugin"));
    } catch (PluginConfigException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    assertEquals(1, SamplePluginMill.size());

    PluginProperties props = SamplePluginMill.getPluginProps("InfluxLPSamplePlugin");
    assertEquals(defaultProps, props.getProperties());
    assertEquals(defaultProps.get("plugin.main"), props.getMain());
    assertEquals(defaultProps.get("plugin.name"), props.getName());
    assertEquals(defaultProps.get("plugin.version"), props.getVersion());
    assertEquals(defaultProps.get("plugin.description"), props.getDescription());
    assertEquals(defaultProps.get("plugin.resultType"), props.getResultType().toString());
    Assertions.assertEquals(PluginType.Sample, props.getType());
    assertEquals(defaultProps.get("plugin.label"), props.getLabel()); // N.B. label is ignored for Samples

    InfluxLPSamplePluginConf conf = new InfluxLPSamplePluginConf("random",
      props.getName() + "Conf", "test/foo",
      Arrays.asList(iConfigTemp, iConfigPress), "testing",
      new HashMap<String,String>(){{put("foo", "foodie"); put("bar", "barfly");}},
      props.getName());

    SamplePlugin sp = SamplePluginMill.genNewInstance(conf);

    ObjectMapper om = new ObjectMapper();

    InfluxLPTestSample ts1 = om.readValue(sp.update().toJson(),
      InfluxLPTestSample.class);

    assertEquals("testing", ts1.measurement);
    assertTrue(ts1.timestamp.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
    assertEquals("foodie", ts1.tags.foo);
    assertEquals("barfly", ts1.tags.bar);
    assertTrue(ts1.fields.temperature > iConfigTemp.getMin() - 3 &&
      ts1.fields.temperature < iConfigTemp.getMax() + 3);
    assertTrue(ts1.fields.pressure > iConfigPress.getMin() - 0.6 &&
      ts1.fields.pressure < iConfigPress.getMax() + 0.6 );

    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));

    InfluxLPTestSample ts2 = om.readValue(sp.update().toJson(), InfluxLPTestSample.class);

    assertNotEquals(ts1.timestamp, ts2.timestamp);
    assertEquals(ts1.measurement, ts2.measurement);
    assertEquals(ts1.tags.foo, ts2.tags.foo);
    assertEquals(ts1.tags.bar, ts2.tags.bar);
    assertNotEquals(ts1.fields.temperature, ts2.fields.temperature);
    assertNotEquals(ts1.fields.pressure, ts2.fields.pressure);

  }

//  @Disabled("for some reason not working now in maven")
  @Test
  public void runSamplePlugin() throws InterruptedException {

    ItemNumConfig iConfigTemp = new ItemNumConfig("temp", "temperature", ItemType.Double, 10, 40, 1, 0.1, 4);
    ItemNumConfig iConfigPress = new ItemNumConfig("press", "pressure", ItemType.Double, 26, 32, 1, 0.1, 4);

    File pluginFile = new File(testJarName);

    try {
      Class<? extends Plugin> clazz = PluginLoader.loadPlugin(pluginFile);
      assertEquals(clazz, SamplePluginMill.getPluginClass("InfluxLPSamplePlugin"));
    } catch (PluginConfigException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException |
             IOException e) {
      throw new RuntimeException(e);
    }

    assertEquals(1, SamplePluginMill.size());

    PluginProperties props = SamplePluginMill.getPluginProps("InfluxLPSamplePlugin");
    InfluxLPSamplePluginConf conf = new InfluxLPSamplePluginConf("random",
      props.getName() + "Conf", "test/foo",
      Arrays.asList(iConfigTemp, iConfigPress), "testing",
      new HashMap<String,String>(){{put("foo", "foodie"); put("bar", "barfly");}},
      props.getName());

    DeviceConfig devConf = new DeviceConfig("random",
      "testDev",
      "testing",
      Arrays.asList(conf),
      1000L,
      0L,
      1);

    Config.init();

    GenericDevice dev = GenericDevice.singleDevice(mockClient, devConf);

    ExecutorService service = Executors.newFixedThreadPool(1);
    service.execute(dev);

    // N.B. returns false if timeout expires, which is what this should do
    assertFalse(service.awaitTermination( Config.ttl(), TimeUnit.MILLISECONDS));

    service.shutdown();

    verify(mockClient, times(1)).connect();
    verify(mockClient, times(10)).publish(eq(conf.getTopic()), anyString());

  }

  @Test
  public void deviceConfigWithSamplePluginDeserializeTest() throws JsonProcessingException, InterruptedException {

    ItemNumConfig iConfigTemp = new ItemNumConfig("temp", "temperature", ItemType.Double, 10, 40, 1, 0.1,4);
    ItemNumConfig iConfigPress = new ItemNumConfig("press", "pressure", ItemType.Double, 26, 32, 1, 0.1, 4);

    File pluginFile = new File(testJarName);

    try {
      Class<? extends Plugin> clazz = PluginLoader.loadPlugin(pluginFile);
      assertEquals(clazz, SamplePluginMill.getPluginClass("InfluxLPSamplePlugin"));
    } catch (PluginConfigException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException |
             IOException e) {
      throw new RuntimeException(e);
    }

    assertEquals(1, SamplePluginMill.size());

    Map<String,String> tags = new HashMap<String,String>(){{put("foo", "foodie"); put("bar", "barfly");}};

    PluginProperties props = SamplePluginMill.getPluginProps("InfluxLPSamplePlugin");
    InfluxLPSamplePluginConf sampleConf = new InfluxLPSamplePluginConf("random",
      props.getName() + "Conf", "test/foo",
      Arrays.asList(iConfigTemp, iConfigPress), "testing",
      tags,
      props.getName());

    DeviceConfig devConf = new DeviceConfig("random",
      "testDev",
      "testing",
      Arrays.asList(sampleConf),
      1000L,
      0L,
      1);

    ObjectMapper omy = new ObjectMapper(new YAMLFactory());
    ObjectMapper omj = new ObjectMapper();

    ObjectWriter ow = omj.writer();

    InfluxLPSamplePluginConfDeserializer ilpdcd = new InfluxLPSamplePluginConfDeserializer();

    String devConfYAML = ow.writeValueAsString(devConf);

    DeviceConfig parsedConfig = omj.readValue(devConfYAML, DeviceConfig.class);

    assertEquals(devConf, parsedConfig);

    // N.B. using mockClient for convenience -
    // in this test deviceRunner is not needed so MQTTClient is not called
    GenericDevice dev = GenericDevice.singleDevice(mockClient, parsedConfig);

    assertEquals(sampleConf.getMeasurement(),
      ((InfluxLPSamplePlugin)dev.getSampleList().get(0)).getMeasurement());

    assertEquals(tags, ((InfluxLPSamplePlugin)dev.getSampleList().get(0)).getTags());

    double temp1 = dev.getSampleList().get(0).getItems().get("temp").get(0).asDouble();
    double press1 = dev.getSampleList().get(0).getItems().get("press").get(0).asDouble();

    assertTrue(temp1 >= iConfigTemp.getMin() - 3 && temp1 <= iConfigTemp.getMax() + 3);
    assertTrue(press1 >= iConfigPress.getMin() - 0.6 && press1 <= iConfigPress.getMax() + 0.6);

    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));

    double temp2 = dev.getSampleList().get(0).getItems().get("temp").get(0).asDouble();
    double press2 = dev.getSampleList().get(0).getItems().get("press").get(0).asDouble();

    assertTrue(temp2 >= iConfigTemp.getMin() - 3 && temp2 <= iConfigTemp.getMax() + 3);
    assertTrue(press2 >= iConfigPress.getMin() - 0.6 && press2 <= iConfigPress.getMax() + 0.6);

  }

  @Test
  public void InfluxLPSamplePluginConfDeserializerTest() throws JsonProcessingException {

    String sampleConfigJSON = "{\"name\":\"InfluxLPSamplePluginConf\",\"id\":\"random\",\"topic\":\"test/foo\",\"items\":[{\"name\":\"temp\",\"label\":\"temperature\",\"type\":\"Double\",\"genClassName\":\"io.bonitoo.qa.data.generator.NumGenerator\",\"max\":40.0,\"min\":10.0,\"period\":1,\"prec\":4},{\"name\":\"press\",\"label\":\"pressure\",\"type\":\"Double\",\"genClassName\":\"io.bonitoo.qa.data.generator.NumGenerator\",\"max\":32.0,\"min\":26.0,\"period\":1,\"prec\":4}],\"plugin\":\"InfluxLPSamplePlugin\",\"measurement\":\"testing\",\"tags\":{\"bar\":\"barfly\",\"foo\":\"foodie\"}}";

    ObjectMapper om = new ObjectMapper();

    InfluxLPSamplePluginConf ilpspconf = om.readValue(sampleConfigJSON, InfluxLPSamplePluginConf.class);

    assertEquals("testing", ilpspconf.getMeasurement());
    assertEquals("test/foo", ilpspconf.getTopic());

    assertTrue(ilpspconf.getTags().containsKey("foo"));
    assertTrue(ilpspconf.getTags().containsKey("bar"));
    assertTrue(ilpspconf.getTags().containsValue("foodie"));
    assertTrue(ilpspconf.getTags().containsValue("barfly"));

  }

}
