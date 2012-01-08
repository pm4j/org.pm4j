package org.pm4j.core.exception;

import org.pm4j.core.pm.PmObject;

/**
 * A runtime exception with resource string based information.
 *
 * @author olaf boede
 */
public class PmResourceRuntimeException extends PmRuntimeException {

  private static final long serialVersionUID = 1L;

  public PmResourceRuntimeException(PmObject pm, String resKey, Object... resStringArgs) {
    super(pm, new PmResourceData(resKey, resStringArgs));
  }

}
