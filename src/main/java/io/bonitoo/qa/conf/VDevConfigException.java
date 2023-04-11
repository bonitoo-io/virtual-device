package io.bonitoo.qa.conf;

/**
 * Exception to be thrown when encountering exceptional states while configuring
 * virtual devices or their runner.
 */
public class VDevConfigException extends IllegalStateException {
  public VDevConfigException(String typeName) {
    super(typeName);
  }
}
