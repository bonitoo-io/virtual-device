package io.bonitoo.qa.conf;

import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @BeforeEach
    public void setup(){
        Config.reset();
    }

    @Test
    public void testReadDeviceConf(){
        String val = Config.getProp("test.val");
        assertEquals("foo", val);
    }

    @Test
    void configFileOutsidePath() throws IOException {
        Properties propsHolder = Config.getProps();
        // zero out props to force new read
        Config.props = null;
        assertNull(Config.props);
        // create a file outside of the resource path
        File testDir = new File("./test-temp");
        testDir.mkdirs();
        File fooFile = new File("./test-temp/foo.config");
        if(!fooFile.exists()){
            fooFile.createNewFile();
        }
        FileWriter writer = new FileWriter(fooFile);
        writer.write("foo=bar");
        writer.close();
        // set config file property to temp file
        String fileHolder = Config.configFile;
        Config.configFile = "./test-temp/foo.config";
        // read from config file outside path
        assertEquals("bar", Config.getProp("foo"));
        assertNull(Config.getProp("test.val"));
        // reset everything to original state
        Config.configFile = fileHolder;
        fooFile.delete();
        testDir.delete();
        Config.props = propsHolder;
        // verify state restored
        assertEquals("foo", Config.getProp("test.val"));
    }

    @Test
    public void getRunnerConfigTest(){
        // Based on testRunnnerConfig.yml as specified in virtualdevice.props runner.conf
        RunnerConfig rConf = Config.getRunnerConfig();
        System.out.println("DEBUG rConf " + rConf);
        assertEquals(10000, rConf.getTtl());
        assertEquals(2, rConf.getDevices().size());
        assertEquals(2, rConf.getDevices().get(0).getSamples().size());
        assertEquals(2, rConf.getDevices().get(1).getSamples().size());
        assertEquals(3, SampleConfigRegistry.keys().size());
        for(String sampKey: SampleConfigRegistry.keys()){
            assertEquals(2, SampleConfigRegistry.get(sampKey).getItems().size());
        }
        assertEquals(6, ItemConfigRegistry.keys().size());
    }

    @Test
    public void runnerConfigOutsideOfPathTest() throws IOException {
        String runnerConfPropHolder = Config.getProp("runner.conf");
        RunnerConfig runnerConfHolder = Config.getRunnerConfig();
        File testDir = new File("./test-temp");
        testDir.mkdirs();
        File fooFile = new File("./test-temp/foo.yml");
        if(!fooFile.exists()){
            fooFile.createNewFile();
        }
        FileWriter writer = new FileWriter(fooFile);
        writer.write("---");
        Config.props.setProperty("runner.conf", "./test-temp/foo.yml");

        // attempt to read fake file throws exception
        assertThrowsExactly(RuntimeException.class, () -> Config.readRunnerConfig(), "com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input");

        // reset state
        fooFile.delete();
        testDir.delete();
        Config.runnerConfig = runnerConfHolder;
        Config.getProps().setProperty("runner.conf", runnerConfPropHolder);

        RunnerConfig rConf = Config.getRunnerConfig();

        assertNotNull(rConf);

    }

    @Test
    public void systemPropsTest(){
        // Based on virtualdevice.props
        Properties propsHolder = Config.getProps();
        assertEquals("1883", propsHolder.get("default.broker.port"));
        assertEquals("localhost", propsHolder.get("default.broker.host"));
        assertEquals("testRunnerConfig.yml", propsHolder.get("runner.conf"));


        System.setProperty("default.broker.port", "10883");
        System.setProperty("default.broker.host", "http://mars.device.portal.mars");
        System.setProperty("runner.conf", "martian.yml");

        Config.props = null;

        Properties newProps = Config.getProps();

        assertEquals("10883", newProps.get("default.broker.port"));
        assertEquals("http://mars.device.portal.mars", newProps.get("default.broker.host"));
        assertEquals("martian.yml", newProps.get("runner.conf"));

        // restore state
        System.clearProperty("default.broker.port");
        System.clearProperty("default.broker.host");
        System.clearProperty("runner.conf");

        Config.props = propsHolder;

    }

}
