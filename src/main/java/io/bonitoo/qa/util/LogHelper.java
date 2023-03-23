package io.bonitoo.qa.util;

public class LogHelper {

    public static String buildMsg(String id, String event, String info){
        return String.format("%s: %s - %s", id, event, info);
    }
}
