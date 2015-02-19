package org.pm4j.core.exception;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;

public class PmRuntimeException extends RuntimeException implements PmUserMessageException {

  /** Default serial version id. */
  private static final long serialVersionUID = 1L;

  /** Container for localization resource key and arguments. */
  private PmResourceData resourceData;

  public PmRuntimeException(PmObject pm, String message, Throwable cause) {
    super(makeMessage(pm, message, cause), cause);
  }

  public PmRuntimeException(PmObject pm, String message) {
    this(pm, message, null);
  }

  public PmRuntimeException(PmObject pm, Throwable cause) {
    this(pm,
         cause != null
            ? cause.getMessage()
            : (String)null,
         cause);
  }

  public PmRuntimeException(PmObject pm, PmResourceData resData) {
    this(pm, resData, null);
  }

  public PmRuntimeException(PmObject pm, PmResourceData resData, Throwable cause) {
    this(pm, resData.toString(), cause);
    setResourceData(resData);
  }

  public PmRuntimeException(String message) {
    super(message);
  }

  public PmRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Ensures that the forwarded exception has some presentation model context information.
   * <p>
   * Simply throws the unchanged exception when it is derived from {@link PmRuntimeException}.<br>
   * Wraps the given exception in a {@link PmRuntimeException} when it is a different exception type.
   *
   * @param pm The presentation model context to add to the exception.
   * @param cause The exception information to forward (rethrow).
   */
  public static void throwAsPmRuntimeException(PmObject pm, Throwable cause) {
    throw asPmRuntimeException(pm, cause);
  }

  /**
   * Ensures that the returned exception has some presentation model context information.
   * <p>
   * Simply returns the unchanged exception when it is derived from {@link PmRuntimeException}.<br>
   * Wraps the given exception in a {@link PmRuntimeException} when it is a different exception type.
   *
   * @param pm The presentation model context to add to the exception.
   * @param cause The exception information.
   * @return An exception with presentation model context information.
   */
  public static PmRuntimeException asPmRuntimeException(PmObject pm, Throwable cause) {
    if (cause instanceof PmRuntimeException) {
      return (PmRuntimeException)cause;
    }
    else {
      return new PmRuntimeException(pm, cause);
    }
  }

  private static String makeMessage(PmObject pm, String message, Throwable cause) {
    if (cause instanceof VirtualMachineError) {
      return message;
    }
    else {
      StringBuilder sb = new StringBuilder(200);
      sb.append(message);
      if (pm != null) {
        sb.append(" - Exception context: Class: '").append(pm.getClass().getName());
        sb.append("' PM: '").append(PmUtil.getPmLogString(pm));
        sb.append('\'');
      }
      return sb.toString();
    }
  }

  public void setResourceData(PmResourceData resourceData) {
    this.resourceData = resourceData;
  }

  public PmResourceData getResourceData() {
    return resourceData;
  }

}
