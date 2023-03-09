package io.bonitoo.qa.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    public void testReadDeviceConf(){
        String val = Config.getProp("test.val");
        assertEquals("foo", val);
    }

}
