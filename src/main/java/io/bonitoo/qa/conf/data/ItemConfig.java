package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.data.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = ItemConfigDeserializer.class)
public class ItemConfig {

    String name;

    ItemType type;

    public ItemConfig(String name, ItemType type) {
        this.name = name;
        this.type = type;
        ItemConfigRegistry.add(this.name, this);
    }

    public ItemConfig ItemConfig(String name){
        return ItemConfigRegistry.get(name);
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            return false;

        if(!(obj instanceof ItemConfig))
            return false;

        final ItemConfig conf = (ItemConfig) obj;

        return (name.equals(conf.name) && type.equals(conf.type));
    }

}
