package org.pm4j.core.pm.impl.converter;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmValidationMessage;
import org.pm4j.core.pm.impl.SetValueContainer;

/**
 * An message that will be generated in case of the PM value type conversion problem.
 *
 * @author olaf boede
 */
public class PmConverterErrorMessage extends PmValidationMessage {

  public PmConverterErrorMessage(PmObject pm, SetValueContainer<?> invalidValue, String msgKey, Object[] msgArgs) {
    super(pm, invalidValue, msgKey, msgArgs);
  }

  public PmConverterErrorMessage(PmObject pm, String msgKey, Object... msgArgs) {
    super(pm, msgKey, msgArgs);
  }

}
