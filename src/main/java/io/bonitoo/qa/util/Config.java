package io.bonitoo.qa.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class Config {

    static final String envConfigFile = "VIRTUAL_DEVICE_CONFIG";
    static Properties props;

    static String configFile =  System.getenv(envConfigFile) == null ? "device.conf" : System.getenv(envConfigFile);

    static private void readProps(){
        props = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream(configFile);
        try {
            props.load(is);
        } catch (IOException e) {
            System.out.println("Unable to load device.conf");
            System.err.println(e);
            System.exit(1);
        }
    }

    static public String getProp(String key){
        if(props == null){
            readProps();
        }
        return props.getProperty(key);
    }

    static public Properties getProps(){
        return props;
    }

    public static String getDeviceID(){
        String deviceID;
        if(getProp("device.id") == null || getProp("device.id").toUpperCase().equals("RANDOM")){
            deviceID = UUID.randomUUID().toString();
            props.setProperty("device.id", deviceID);
        }
        System.out.println("DEBUG device.id " + getProp("device.id"));
        return getProp("device.id");
    }
}
