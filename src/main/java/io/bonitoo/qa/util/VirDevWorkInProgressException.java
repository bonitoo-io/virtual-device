package io.bonitoo.qa.util;

/**
 * Exception to be used with methods that are not yet implemented,
 * but may be needed as placeholders or reminders for future work.
 */
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
