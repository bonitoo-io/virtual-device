package io.bonitoo.qa;

/**
 * Encapsulates Virtual Device exceptions that occur during runtime.
 */
public class VirDevRuntimeException extends RuntimeException {
  public VirDevRuntimeException(String s) {
    super(s);
  }
}
