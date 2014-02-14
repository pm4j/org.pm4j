package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;

/**
 * A specific {@link PmMessage} that is used to show validation error messages.
 *
 * @author Olaf Boede
 */
public class PmValidationMessage extends PmMessage {

  /** TODO: Docu for invalid value management. */
  private final WeakReference<SetValueContainer<?>> invalidValueRef;

  public PmValidationMessage(PmObject pm, SetValueContainer<?> invalidValue, Throwable cause, String msgKey, Object... msgArgs) {
    super(pm, Severity.ERROR, cause, msgKey, msgArgs);
    invalidValueRef = new WeakReference<SetValueContainer<?>>(invalidValue);
  }

  public PmValidationMessage(PmObject pm, String msgKey, Object... msgArgs) {
    this(pm, (Throwable)null, msgKey, msgArgs);
  }

  @SuppressWarnings("unchecked")
  public PmValidationMessage(PmObject pm, Throwable cause, String msgKey, Object... msgArgs) {
    super(pm, Severity.ERROR, cause, msgKey, msgArgs);
    if (pm instanceof PmAttr) {
      PmAttr<?> attr = (PmAttr<?>)pm;
      @SuppressWarnings("rawtypes")
      SetValueContainer c = new SetValueContainer(attr);
      c.setPmValue(attr.getValue());
      invalidValueRef = new WeakReference<SetValueContainer<?>>(c);
    }
    else {
      invalidValueRef = null;
    }
  }

  public WeakReference<SetValueContainer<?>> getInvalidValueRef() {
    return invalidValueRef;
  }
}
