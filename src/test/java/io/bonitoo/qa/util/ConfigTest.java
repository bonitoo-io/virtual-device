package io.bonitoo.qa.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Test
    public void testReadDeviceConf(){
        String val = Config.getProp("test.val");
        assertEquals("foo", val);
    }

    @Test
    public void consistentDeviceID(){
        String deviceID = Config.getDeviceID();
        assertNotNull(deviceID);
        assertEquals(deviceID, Config.getProp("device.id"));
        assertEquals(deviceID, Config.getDeviceID());
    }

    @Test void configFileOutsidePath() throws IOException {
        Properties propsHolder = Config.getProps();
        // zero out props to force new read
        Config.props = null;
        assertNull(Config.getProps());
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

}
