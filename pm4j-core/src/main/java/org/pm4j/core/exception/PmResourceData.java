package org.pm4j.core.exception;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmExceptionHandlerImpl;
import org.pm4j.core.pm.impl.PmUtil;


/**
 * A string resource data container for localizeable exceptions.
 * <p>
 * The code that catches such an exception (usually an {@link PmExceptionHandlerImpl})
 * may use the resouce key to provide a localized error message.
 *
 * @author olaf boede
 */
public class PmResourceData {

  /** Default serial version id. */
  private static final long serialVersionUID = 1L;

  public final String msgKey;
  public final Object[] msgArgs;
  public final PmObject pm;

  public PmResourceData(PmObject pm, String msgKey, Object... msgArgs) {
    this.pm = pm;
    this.msgKey = msgKey;
    this.msgArgs = msgArgs;
  }

  public PmResourceData(String msgKey, Object... msgArgs) {
    this(null, msgKey, msgArgs);
  }

  @Override
  public String toString() {
    return msgKey +
           (msgArgs.length > 0 ? " args: " + msgArgs : "") +
           (pm != null ? " pm=" + PmUtil.getPmLogString(pm) : "");
  }
}
