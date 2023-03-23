package io.bonitoo.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bonitoo.qa.conf.RunnerConfig;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemConfigDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class Sandbox {

    static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static public void main(String[] args) throws IOException {



        System.out.println("Attempt to read yaml");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File file = new File(loader.getResource("test.yml").getFile());

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(ItemConfig.class, new ItemConfigDeserializer());
        om.registerModule(sm);
        // om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //DeviceConfig deviceCfg = om.readValue(file, DeviceConfig.class);

        //System.out.println("DEVICE info " + deviceCfg.toString());
     /*   Map<String, Object> map = om.readValue(file, new TypeReference<Map<String,Object>>() {});
        map.keySet().forEach(key -> {
           System.out.println(String.format("%s:%s", key, map.get(key)));
        });

        System.out.println(((List)map.get("broker")).get(0)); */
      //  RunnerConfig testCfg = om.readValue(file, RunnerConfig.class);

    ///    System.out.println(testCfg.name);
     //   System.out.println(testCfg);
       // System.setProperty("org.slf4j.simpleLogger.logFile", "test.log");
        logger.info("Shutting down");

     //   File confFile = new File(loader.getResource("device.conf").getFile());
     //   RunnerConfig testCfg2 = om.readValue(confFile, RunnerConfig.class);
    }
}
