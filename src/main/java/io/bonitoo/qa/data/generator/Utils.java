package io.bonitoo.qa.data.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Location for utilities used in working with data.
 *
 * <p>N.B. this was used in the first POC iteration of this project
 * and will likely be replaced.
 */
public class Utils {

  public static String pojoToJson(Object pojo) throws JsonProcessingException {
    ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return objectWriter.writeValueAsString(pojo);
  }

}
