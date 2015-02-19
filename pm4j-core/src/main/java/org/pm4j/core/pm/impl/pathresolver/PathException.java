package org.pm4j.core.pm.impl.pathresolver;

public class PathException extends RuntimeException {

  private static final long serialVersionUID = -8443239101218179335L;

  public PathException(String path, String msg) {
    this(path, msg, null);
  }

  public PathException(String path, String msg, Throwable cause) {
    super("Unable to process path '" + path + "': " + msg, cause);
  }
}
