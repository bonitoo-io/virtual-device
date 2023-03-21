package io.bonitoo.qa.data;

import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import io.bonitoo.qa.conf.data.ItemStringConfig;
import io.bonitoo.qa.data.generator.Generator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Item {

    static double precision = 1e3;
    Object val;

    public static double setPrecision(double prec){
        precision = prec;
        return precision;
    }

    public static double getPrecision(){
        return precision;
    }

    static public Item of(ItemConfig iConf){

        Item it;

        switch(iConf.getType()){
            case BuiltInTemp:
                it = new Item(Generator.genTemperature(System.currentTimeMillis()));
                break;
            case Double:
                it = new Item(Generator.precision(Generator.genDoubleVal(
                        ((ItemNumConfig)iConf).getPeriod(),
                        ((ItemNumConfig)iConf).getMin(),
                        ((ItemNumConfig)iConf).getMax(),
                        System.currentTimeMillis()), precision));
                break;
            case Long:
                it = new Item((long)Generator.genDoubleVal(
                        ((ItemNumConfig)iConf).getPeriod(),
                        ((ItemNumConfig)iConf).getMin(),
                        ((ItemNumConfig)iConf).getMax(),
                        System.currentTimeMillis()));
                break;
            case String:
                List<String> values = ((ItemStringConfig)iConf).getValues();
                 String value = values.get((int)Math.floor(Math.random() * values.size()));
                it = new Item(value);
                break;
            default:
                throw new RuntimeException("Unsupported Type " + iConf.getType());
        }

        return it;
    }

    public Double asDouble(){
        if(val instanceof Double){
            return (Double)val;
        }
        return null;
    }

    public Long asLong(){
        if(val instanceof Long){
            return (Long)val;
        }
        return null;
    }

    public String asString(){
        if(val instanceof String){
            return (String)val;
        }
        return null;
    }


}
