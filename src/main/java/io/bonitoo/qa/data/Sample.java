package io.bonitoo.qa.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public abstract class Sample {

    public String id;

    @JsonIgnore //should not be part of payload
    public String topic;

    public long timestamp;

    @JsonIgnore // use only flattened values
    @JsonAnyGetter // flatten values
    public Map<String, Object> items;

    public Object item(String name){
        return items.get(name);
    }

    @Override
    public String toString(){
        checkNameClash();
        String result = String.format("id=%s,timestamp=%d,items=[", id, timestamp);
        for(String key: items.keySet()){
            if(items.get(key) instanceof Double){
                result += String.format("name:%s,val:%.2f,", key, items.get(key));
            }else if(items.get(key) instanceof String){
                result += String.format("name:%s,val:%s,", key, items.get(key));
            }else{
                result += String.format("name:%s,val:%d,", key, items.get(key));
            }
        }
        return result += "]\n";
    }

    public void checkNameClash(){

        List<String> toRemove = new ArrayList<>();

        for(String item: items.keySet()){
            for(Field f : Sample.class.getDeclaredFields()){
                if(f.getName().toUpperCase().equals(item.toUpperCase())){
                    toRemove.add(item);
                }
            }
        }

        for(String key: toRemove){
            System.out.println(String.format("WARNING: Item field name %s not allowed, item removed from sample list", key));
            items.remove(key);
        }
    }

    public String toJson() throws JsonProcessingException {
        checkNameClash();
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return objectWriter.writeValueAsString(this);
    }
}
