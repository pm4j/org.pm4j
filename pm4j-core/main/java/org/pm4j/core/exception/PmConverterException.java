package org.pm4j.core.exception;

import org.pm4j.core.pm.PmObject;

public class PmConverterException extends PmValidationException {

  private static final long serialVersionUID = 7800618523480806486L;

  public PmConverterException(PmObject pm, String msgKey, Object... msgArgs) {
    super(pm, msgKey, msgArgs);
  }

}
