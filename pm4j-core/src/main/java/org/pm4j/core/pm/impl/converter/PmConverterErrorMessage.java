package org.pm4j.core.pm.impl.converter;

import org.pm4j.core.exception.PmUserMessageException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmValidationMessage;
import org.pm4j.core.pm.impl.SetValueContainer;

/**
 * An message that will be generated in case of the PM value type conversion problem.
 *
 * @author Olaf Boede
 */
public class PmConverterErrorMessage extends PmValidationMessage {

  public PmConverterErrorMessage(PmObject pm, SetValueContainer<?> invalidValue, Throwable cause, String msgKey, Object[] msgArgs) {
    super(pm, invalidValue, cause, msgKey, msgArgs);
  }

  public PmConverterErrorMessage(PmObject pm, SetValueContainer<?> invalidValue, PmUserMessageException cause) {
    this(pm, invalidValue, (Throwable)cause, cause.getResourceData().getMsgKey(), cause.getResourceData().getMsgArgs());
  }

  public PmConverterErrorMessage(PmObject pm, Throwable cause, String msgKey, Object... msgArgs) {
    super(pm, cause, msgKey, msgArgs);
  }

}
