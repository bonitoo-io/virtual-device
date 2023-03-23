package io.bonitoo.qa.conf;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.RunnerConfig;
import io.bonitoo.qa.conf.VirtualDeviceConfigException;
import io.bonitoo.qa.conf.data.SampleConfig;
import io.bonitoo.qa.conf.data.ItemConfigRegistry;
import io.bonitoo.qa.conf.data.SampleConfigRegistry;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.AuthConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class Config {

    static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final String envConfigFile = "VIRTUAL_DEVICE_CONFIG";

    static RunnerConfig runnerConfig;
    static Properties props;

    static String configFile =  System.getenv(envConfigFile) == null ? "virtualdevice.props" : System.getenv(envConfigFile).trim();

    static private void readProps(){
        props = new Properties();
        try{
            logger.info(LogHelper.buildMsg("config", "Reading base config", configFile));
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream(configFile) == null ?
                new FileInputStream(new File(configFile)) :
                loader.getResourceAsStream(configFile);
            props.load(is);
        } catch (IOException e) {
            logger.error(LogHelper.buildMsg("config", "Load failure", String.format("Unable to load config file %s", configFile)));
            logger.error(LogHelper.buildMsg("config", "Load exception", e.toString()));
            System.exit(1);
        }

        // Overwrite properties set in JVM with system values
        for(String key: props.stringPropertyNames()){
            if(System.getProperty(key) != null){
                props.setProperty(key, System.getProperty(key));
            }
        }
    }

    static protected void readRunnerConfig(){
        if(props == null){
            readProps();
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        logger.info(LogHelper.buildMsg("config", "Reading runner config", props.getProperty("runner.conf")));

        URL runnerConfResourceFile = loader.getResource(props.getProperty("runner.conf"));

        File confFile = runnerConfResourceFile == null ?
                new File(props.getProperty("runner.conf")) :
                new File(runnerConfResourceFile.getFile());
        try{
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            runnerConfig = om.readValue(confFile, RunnerConfig.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static public String getProp(String key){
        if(props == null){
            readProps();
        }
        return props.getProperty(key);
    }

    static public Properties getProps(){
        if(props == null){
            readProps();
        }
        return props;
    }

    static public BrokerConfig getBrokerConf(){
        if(runnerConfig == null){
            readRunnerConfig();
        }
        return runnerConfig.getBroker();
    }

    static public List<SampleConfig> getSampleConfs(int ofDev){
        if(runnerConfig == null){
            readRunnerConfig();
        }
        return runnerConfig.getDevices().get(ofDev).getSamples();
    }

    static public List<DeviceConfig> getDeviceConfs(){
        if(runnerConfig == null){
            readRunnerConfig();
        }
        return runnerConfig.getDevices();
    }

    static public RunnerConfig getRunnerConfig(){
        if(runnerConfig == null){
            readRunnerConfig();
        }
        return runnerConfig;
    }

    static public Long TTL(){
        return runnerConfig.getTtl();
    }

    static public DeviceConfig deviceConf(int i){
        return getDeviceConfs().get(i);
    }

    static public SampleConfig sampleConf(int ofDev, int i){
        return getSampleConfs(ofDev).get(i);
    }

    //alias for above
    static public BrokerConfig brokerConf(){
        return getBrokerConf();
    }

    static public void reset(){
        runnerConfig = null;
        ItemConfigRegistry.clear();
        SampleConfigRegistry.clear();
        runnerConfig = getRunnerConfig();
    }
}
