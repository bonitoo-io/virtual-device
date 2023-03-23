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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class Config {

    static final String envConfigFile = "VIRTUAL_DEVICE_CONFIG";

    static RunnerConfig runnerConfig;
    static Properties props;

    static String configFile =  System.getenv(envConfigFile) == null ? "device.conf" : System.getenv(envConfigFile).trim();

    static private void readProps(){
        props = new Properties();
        try{
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream(configFile) == null ?
                new FileInputStream(new File(configFile)) :
                loader.getResourceAsStream(configFile);
            props.load(is);
        } catch (IOException e) {
            System.out.println(String.format("Unable to load config file %s", configFile));
            System.err.println(e);
            System.exit(1);
        }
    }

    // TODO update to work with genericDevice

    static private void readConfFile(){
        readProps();
        AuthConfig authConfig = new AuthConfig(props.getProperty("broker.username"),
                props.getProperty("broker.password"));
        BrokerConfig brokerConfig = new BrokerConfig(props.getProperty("broker.host"),
                Integer.parseInt(props.getProperty("broker.port")),
                authConfig);
        List<SampleConfig> samplesConfig = new ArrayList<>();
        List<DeviceConfig> devicesConfig = new ArrayList<>();
        String sampleId = props.getProperty("sample.id") == null || props.getProperty("sample.id").equals("device.id") ?
                props.getProperty("device.id") : props.getProperty("sample.id");
        samplesConfig.add(new SampleConfig(sampleId, props.getProperty("sample.name"),
                props.getProperty("sample.topic"),
                (String[])null));
        devicesConfig.add(new DeviceConfig(props.getProperty("device.id"),
                props.getProperty("device.name"),
                props.getProperty("device.description"),
               samplesConfig,
               Long.parseLong(props.getProperty("device.interval")),
               0l,
               1
        ));

        runnerConfig = new RunnerConfig(brokerConfig, devicesConfig, Long.parseLong(props.getProperty("runner.ttl")));

    }

    static private void readRunnerConfig(){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File confFile = new File(loader.getResource(configFile).getFile());
        try{
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            runnerConfig = om.readValue(confFile, RunnerConfig.class);
        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (VirtualDeviceConfigException e){
            // not a yaml file
            System.out.println(e.getMessage());
            readConfFile();
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO remove and update usages
    static public String getProp(String key){
        if(props == null){
            readProps();
        }
        return props.getProperty(key);
    }

    static public Properties getProps(){
        return props;
    }

    // Todo Use and Test the following
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

    static public String getDeviceID(){
        return getProp("device.id");
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
