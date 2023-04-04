package io.bonitoo.qa.conf;

/**
 * Exception to be thrown when encountering exceptional states while configuring
 * virtual devices or their runner.
 */
public class VirtualDeviceConfigException extends IllegalStateException {
  public VirtualDeviceConfigException(String typeName) {
    super(typeName);
  }
}
