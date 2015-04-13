package org.pm4j.core.exception;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmExceptionHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A string resource data container for localizeable exceptions.
 * <p>
 * The code that catches such an exception (usually an {@link PmExceptionHandlerImpl})
 * may use the resouce key to provide a localized error message.
 *
 * @author olaf boede
 */
public class PmResourceData {
  private static final Logger LOG = LoggerFactory.getLogger(PmResourceData.class);

  /** Default serial version id. */
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1L;

  /** @deprecated Please use {@link #getMsgKey()}. */
  public final String msgKey;
  /** @deprecated Please use {@link #getMsgArgs()}. */
  public final Object[] msgArgs;
  /** @deprecated Please use {@link #getPm()}. */
  public final PmObject pm;

  public PmResourceData(PmObject pm, String msgKey, Object... msgArgs) {
    this.pm = pm;
    this.msgKey = msgKey;

    if (pm != null &&
        (msgArgs.length == 0 ||
         ! (msgArgs[msgArgs.length-1] instanceof LazyPmObjectTitleProvider))
       ) {
      this.msgArgs = Arrays.copyOf(msgArgs, msgArgs.length+1);
      this.msgArgs[msgArgs.length] = new LazyPmObjectTitleProvider();
    } else {
      this.msgArgs = msgArgs;
    }
  }

  public PmResourceData(String msgKey, Object... msgArgs) {
    this(null, msgKey, msgArgs);
  }

  @Override
  public String toString() {
    if (pm != null) {
      String s = PmLocalizeApi.findLocalization(pm, msgKey, msgArgs);
      if (StringUtils.isNotBlank(s)) {
        return s;
      }
    }
    // fall back:
    return msgKey + (msgArgs.length > 0 ? " args: " + msgArgs : "");
  }

  /**
   * @return the msgKey
   */
  public final String getMsgKey() {
    return msgKey;
  }

  /**
   * @return the msgArgs
   */
  public final Object[] getMsgArgs() {
    return msgArgs;
  }

  /**
   * @return the pm
   */
  public final PmObject getPm() {
    return pm;
  }

 /** The title gets provided by a proxy to prevent problems with PM initialization states.
   * It also prevents unnecessary getPmTitle() calls if the title is not relevant for the
   * resource string. */
 class LazyPmObjectTitleProvider {

   @Override
    public String toString() {
      try {
        return pm.getPmTitle();
      }
      catch (RuntimeException e) {
        // a fall back if the title is not accessible:
        LOG.info("Unable to resolve a title parameter for a message. Related PM: " + pm.getPmRelativeName());
        return pm.getPmRelativeName();
      }
    }
  }

}
