package io.bonitoo.qa.conf.data;

import io.bonitoo.qa.data.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemStringConfig extends ItemConfig {

    List<String> values;

    public ItemStringConfig(String name, ItemType type, List<String> values) {
        super(name, type);
        this.values = values;
        ItemConfigRegistry.add(this.name, this);
    }

    @Override
    public String toString(){
        String result = String.format("name:%s,type:%s,values:[", name, type);
        for(String val : values){
            result += String.format("%s,",val);
        }
        result += "]\n";
        return result;
    }

    @Override
    public boolean equals(Object obj){

        if(! super.equals(obj))
            return false;

        final ItemStringConfig conf = (ItemStringConfig) obj;

        if(values.size() != conf.values.size())
            return false;

        for(String s : values){
            if(!conf.values.contains(s))
                return false;
        }

        for(String s : conf.values){
            if(!values.contains(s))
                return false;
        }

        return true;

    }
}
