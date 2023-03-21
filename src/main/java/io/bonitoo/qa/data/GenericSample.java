package io.bonitoo.qa.data;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.SampleConfig;

import java.util.HashMap;

public class GenericSample extends Sample{

    static public GenericSample of(SampleConfig sConf){
        GenericSample gs = new GenericSample();
        gs.id = sConf.getId();
        gs.topic = sConf.getTopic();
        gs.items = new HashMap<>();
        for(ItemConfig iConf: sConf.getItems()){
            gs.getItems().put(iConf.getName(), Item.of(iConf).getVal());
        }
        gs.timestamp = System.currentTimeMillis();

        return gs;
    }
}
