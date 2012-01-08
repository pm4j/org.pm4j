package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;

public class PmValidationMessage extends PmMessage {

  private final WeakReference<SetValueContainer<?>> invalidValueRef;

  public PmValidationMessage(PmObject pm, SetValueContainer<?> invalidValue, String msgKey, Object... msgArgs) {
    super(pm, Severity.ERROR, msgKey, msgArgs);
    invalidValueRef = new WeakReference<SetValueContainer<?>>(invalidValue);
  }

  @SuppressWarnings("unchecked")
  public PmValidationMessage(PmObject pm, String msgKey, Object... msgArgs) {
    super(pm, Severity.ERROR, msgKey, msgArgs);
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
