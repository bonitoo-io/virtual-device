package io.bonitoo.qa.conf;

/**
 * Exception to be thrown when encountering exceptional states while configuring
 * virtual devices or their runner.
 */
public class VirDevConfigException extends IllegalStateException {
  public VirDevConfigException(String typeName) {
    super(typeName);
  }
}
