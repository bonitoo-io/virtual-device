package io.bonitoo.qa.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * A Sample holds any number of items the values of which can be randomly generated.
 * It can then be serialized to an MQTT message.  The files timestamp and items are
 * serialized to JSON as the message payload.  The topic field is reserved for the MQTT
 * topic header.
 */
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

  public Object item(String name) {
    return items.get(name);
  }

  @Override
  public String toString() {
    checkNameClash();
    StringBuilder result = new StringBuilder(
        String.format("id=%s,timestamp=%d,items=[", id, timestamp)
    );
    for (String key : items.keySet()) {
      if (items.get(key) instanceof Double) {
        result.append(String.format("name:%s,val:%.2f,", key, (Double) items.get(key)));
      } else if (items.get(key) instanceof String) {
        result.append(String.format("name:%s,val:%s,", key, items.get(key)));
      } else {
        result.append(String.format("name:%s,val:%d,", key, (Long) items.get(key)));
      }
    }
    return result.append("]\n").toString();
  }

  private void checkNameClash() {

    List<String> toRemove = new ArrayList<>();

    for (String item : items.keySet()) {
      for (Field f : Sample.class.getDeclaredFields()) {
        if (f.getName().equalsIgnoreCase(item)) {
          toRemove.add(item);
        }
      }
    }

    for (String key : toRemove) {
      System.out.printf(
          "WARNING: Item field name %s not allowed, item removed from sample list%n", key
      );
      items.remove(key);
    }
  }

  /**
   * Serializes the Sample instance to a JSON string.
   *
   * @return - a JSON string.
   * @throws JsonProcessingException - thrown when object cannot be serialized.
   */
  public String toJson() throws JsonProcessingException {
    checkNameClash();
    // todo add pretty print option.
    ObjectWriter objectWriter = new ObjectMapper().writer(); //.withDefaultPrettyPrinter();
    return objectWriter.writeValueAsString(this);
  }
}
