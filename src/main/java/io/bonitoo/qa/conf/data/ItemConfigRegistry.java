package io.bonitoo.qa.conf.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemConfigRegistry {

    static Map<String, ItemConfig> registry = new HashMap<>();

    static public void add(String key, ItemConfig item){
        registry.put(key, item);
    }

    static public ItemConfig get(String key){

        ItemConfig item = registry.get(key);

        if(item == null){
            throw new RuntimeException(String.format("Item Configuration named %s not found", key));
        }

        return registry.get(key);

    }

    static public void clear(){
        registry.clear();
    }

    static public Set<String> keys(){
        return registry.keySet();
    }

}
