package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = SampleConfigDeserializer.class)
public class SampleConfig {

    String id;

    String name;

    String topic;

    List<ItemConfig> items;

    public static String resolveID(String id){
        if(id.toUpperCase().equals("RANDOM"))
            return UUID.randomUUID().toString();
        else
            return id;
    }

    public SampleConfig(String id, String name, String topic, List<ItemConfig> items){
        this.id = resolveID(id);
        this.name = name;
        this.topic = topic;
        this.items = items;
        SampleConfigRegistry.add(this.name, this);
    }

    public SampleConfig(String id, String name, String topic, String[] itemNames){
        this.id = resolveID(id);
        this.name = name;
        this.topic = topic;
        items = new ArrayList<>();
        if(itemNames != null) {
            for (String itemName : itemNames) {
                items.add(ItemConfigRegistry.get(itemName));
            }
        }
        SampleConfigRegistry.add(this.name, this);
    }

    public SampleConfig(SampleConfig sampleConfig){
       this.id = sampleConfig.getId();
       this.name = sampleConfig.getName();
       this.topic = sampleConfig.getTopic();
       this.items =  sampleConfig.getItems();
    }

    /*
    public String getId(){
        if(id == null || id.toUpperCase().equals("RANDOM")){
            id = UUID.randomUUID().toString();
        }
        return id;
    }*/

    @Override
    public String toString(){
        String result = String.format("id=%s,name=%s,topic=%s,items[\n", id,name,topic);
        if(items != null){
            for(ItemConfig item: items){
                result += String.format("Item:%s", item);
            }
        }
       result += "]\n";
       return result;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            return false;

        if(! (obj instanceof SampleConfig))
            return false;

        final SampleConfig conf = (SampleConfig)obj;

        if(!(name.equals(conf.name) &&
                id.equals(conf.id) &&
                topic.equals(conf.topic)))
            return false;

        for(ItemConfig itemConfig : items){
            if(!conf.items.contains(itemConfig))
                return false;
        }

        for(ItemConfig itemConfig : conf.items){
            if(!items.contains(itemConfig))
                return false;
        }

        return true;
    }

}
