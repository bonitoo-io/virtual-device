package io.bonitoo.qa.conf.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bonitoo.qa.conf.data.SampleConfig;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonDeserialize(using = DeviceConfigDeserializer.class)
public class DeviceConfig {

 //   String topic;

    private String id;

    String name;

    String description;

    List<SampleConfig> samples;

    Long interval;

    Long jitter = 0l;

//    String sampleName;

    int count;

    public DeviceConfig(DeviceConfig deviceConfig, int number){
        this.id = String.format("%s-%03d", deviceConfig.getId(), number);
        this.name = String.format("%s-%03d", deviceConfig.getName(), number);
        this.description = deviceConfig.getDescription();
        this.interval = deviceConfig.getInterval();
        this.jitter = deviceConfig.getJitter();
        this.count = 1;
        this.samples = new ArrayList<>();
        // deep copy sample configs ana modify
        for(SampleConfig sampleConfig : deviceConfig.getSamples()){
            SampleConfig newConf = new SampleConfig(sampleConfig);
            newConf.setId(String.format("%s-%03d", sampleConfig.getId(), number));
            newConf.setName(String.format("%s-%03d", sampleConfig.getName(), number));
            this.samples.add(newConf);
        }
    }

    public String getId(){
        if(id == null || id.toUpperCase().equals("RANDOM")){
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public SampleConfig getSample(int index){
        return samples.get(index);
    }
    public SampleConfig getSample(String name) {
        for(SampleConfig sample: samples){
            if(sample.getName().equals(name)){
                return sample;
            }
        }
        return null;
    }

    @Override
    public String toString(){
        String result = String.format("name=%s,id=%s,description=%s,interval=%d,jitter=%d,count=%d,\nsamples=[\n",
                name, getId(), description, interval, jitter, count);
        for(SampleConfig sample : samples){
            result += String.format("%s", sample);
        }
        result += "]\n";
        return result;
    }

    @Override
    public boolean equals(Object obj){

        if(obj == null)
            return false;

        if(!(obj instanceof DeviceConfig))
            return false;

        final DeviceConfig conf = (DeviceConfig)obj;

        if(!(conf.id.equals(id) &&
                conf.name.equals(name) &&
                conf.interval.equals(interval) &&
                conf.jitter.equals(jitter) &&
                conf.count == count))
            return false;

        for(SampleConfig sample : samples){
            if(!conf.samples.contains(sample))
                return false;
        }

        for(SampleConfig sample : conf.samples){
            if(!samples.contains(sample))
                return false;
        }

        return true;
    }
}
