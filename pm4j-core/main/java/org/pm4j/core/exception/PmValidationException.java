package org.pm4j.core.exception;

import org.pm4j.core.pm.PmObject;

/**
 * Presentation model validation exception.
 *
 * @author olaf boede
 */
public class PmValidationException extends PmException {

  /** Default serial version id. */
  private static final long serialVersionUID = 1L;

  public PmValidationException(PmObject pm, String msgKey, Object... msgArgs) {
    super(pm, msgKey, msgArgs);
  }

  public PmValidationException(PmResourceData resourceData) {
    super(resourceData, null);
  }

}
