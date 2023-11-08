package io.bonitoo.qa.util;

public class VirDevWorkInProgressException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public VirDevWorkInProgressException() {
  }

  public VirDevWorkInProgressException(String message) {
    super(message);
  }

  public VirDevWorkInProgressException(String message, Throwable cause) {
    super(message, cause);
  }
}
