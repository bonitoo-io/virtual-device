package io.bonitoo.qa.conf.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SampleConfigRegistry {

    static Map<String, SampleConfig> registry = new HashMap<>();

    static public void add(String key, SampleConfig sample){
        registry.put(key, sample);
    }

    static public SampleConfig get(String key){

        SampleConfig sample = registry.get(key);

        if(sample == null)
            throw new RuntimeException(String.format("Sample Configuration named %s not found", key));

        return registry.get(key);
    }

    static public void clear(){
        registry.clear();
    }

    static public Set<String> keys(){
        return registry.keySet();
    }
}
