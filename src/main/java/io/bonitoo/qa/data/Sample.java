package io.bonitoo.qa.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.bonitoo.qa.conf.data.SampleConfig;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Sample holds any number of items the values of which can be randomly generated.
 * It can then be serialized to an MQTT message.  The files timestamp and items are
 * serialized to JSON as the message payload.  The topic field is reserved for the MQTT
 * topic header.
 */
@Setter
@Getter
public abstract class Sample {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public String id;

  @JsonIgnore //should not be part of payload
  public String topic;

  public long timestamp;

  @JsonIgnore // use only flattened values
  @JsonAnyGetter // flatten values
  public Map<String, Item> items;

  public abstract Sample update();

  public static Sample of(Function<SampleConfig, Sample> init, SampleConfig config) {
    return init.apply(config);
  }

  public Item item(String name) {
    return items.get(name);
  }

  public Object itemVal(String name) {
    return items.get(name).getVal();
  }

  @Override
  public String toString() {
    checkNameClash();
    StringBuilder result = new StringBuilder(
        String.format("id=%s,timestamp=%d,items=[", id, timestamp)
    );
    for (String key : items.keySet()) {
      if (items.get(key).getVal() instanceof Double) {
        result.append(String.format("name:%s,val:%.2f,", key, items.get(key).asDouble()));
      } else if (items.get(key).getVal() instanceof String) {
        result.append(String.format("name:%s,val:%s,", key, items.get(key)));
      } else {
        result.append(String.format("name:%s,val:%d,", key, items.get(key).asLong()));
      }
    }
    return result.append("]\n").toString();
  }

  protected void checkNameClash() {

    List<String> toRemove = new ArrayList<>();

    for (String item : items.keySet()) {
      for (Field f : Sample.class.getDeclaredFields()) {
        if (f.getName().equalsIgnoreCase(item)) {
          toRemove.add(item);
        }
      }
    }

    for (String key : toRemove) {
      logger.warn(String
          .format("Item field name %s not allowed, item removed from sample list.",
            key));
      items.remove(key);
    }
  }

  /**
   * Serializes the Sample instance to a JSON string.
   *
   * @return - a JSON string.
   * @throws JsonProcessingException - thrown when object cannot be serialized.
   */
  /*  public String toJson() throws JsonProcessingException {
    checkNameClash();
    // todo add pretty print option.
    ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return objectWriter.writeValueAsString(this);
  } */
  public abstract String toJson() throws JsonProcessingException;
}
