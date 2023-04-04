package io.bonitoo.qa.util;

/**
 * Contains methods to help maintains standard reporting structures when writing log messages.
 */
public class LogHelper {

  public static String buildMsg(String id, String event, String info) {
    return String.format("%s: %s - %s", id, event, info);
  }
}
