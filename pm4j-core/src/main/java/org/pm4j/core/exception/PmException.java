package org.pm4j.core.exception;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.pm.PmObject;

/**
 * @author olaf boede
 */
public class PmException extends Exception implements PmUserMessageException {

  /** Default serial version id. */
  private static final long serialVersionUID = 1L;

  /** Container for localization resource key and arguments. */
  private PmResourceData resourceData;

  public PmException(PmObject pm, String msgKey, Object... msgArgs) {
    super(msgKey + " on " + pm + " " + msgArgs);
    this.resourceData = new PmResourceData(pm, msgKey, msgArgs);
  }

  public PmException(PmResourceData resourceData, Throwable cause) {
    super(ObjectUtils.toString(resourceData), cause);
    this.resourceData = resourceData;
  }


  public PmResourceData getResourceData() {
    return resourceData;
  }

  public void setResourceData(PmResourceData resourceData) {
    this.resourceData = resourceData;
  }

}
