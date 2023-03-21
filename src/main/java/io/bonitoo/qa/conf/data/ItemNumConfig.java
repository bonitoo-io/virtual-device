package io.bonitoo.qa.conf.data;

import io.bonitoo.qa.data.ItemType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemNumConfig extends ItemConfig {

    double max;
    double min;
    long period;

    public ItemNumConfig(String name, ItemType type, double min, double max, long period) {
        super(name, type);
        this.max = max;
        this.min = min;
        this.period = period;
        ItemConfigRegistry.add(this.name, this);
    }

    @Override
    public String toString(){
        return String.format("name=%s,max=%.2f,min=%.2f,period=%d,type=%s\n",
                name, max, min, period, type);
    }

    @Override
    public boolean equals(Object obj){
        if(!super.equals(obj))
            return false;
        final ItemNumConfig conf = (ItemNumConfig) obj;
        return max == conf.max && min == conf.min && period == conf.period;
    }
}
