package io.bonitoo.qa;

/**
 * Encapsulates Virtual Device exceptions that occur during runtime.
 */
public class VirtualDeviceRuntimeException extends RuntimeException {
  public VirtualDeviceRuntimeException(String s) {
    super(s);
  }
}
