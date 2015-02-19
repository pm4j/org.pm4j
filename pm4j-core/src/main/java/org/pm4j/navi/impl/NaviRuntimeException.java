package org.pm4j.navi.impl;

public class NaviRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -1365695584818020668L;

  public NaviRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public NaviRuntimeException(String message) {
    super(message);
  }

  public NaviRuntimeException(Throwable cause) {
    super(cause);
  }

}
