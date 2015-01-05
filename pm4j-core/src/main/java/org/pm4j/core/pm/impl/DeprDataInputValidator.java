package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmMessageApi;

/**
 * Implements the validation logic that was formerly in {@link PmDataInputBase#pmValidate()}.
 */
class DeprDataInputValidator<T extends PmObjectBase> implements PmObjectBase.Validator {
  @SuppressWarnings("unchecked")
  @Override
  public void validate(PmObject pm) {
    // XXX oboede: If visibility should be considered should be an
    // internal decision of the PM specific logic.
    if (pm.isPmVisible() && !pm.isPmReadonly()) {
      try {
        validateImpl((T)pm);
      } catch (PmValidationException e) {
        PmMessageApi.addMessage(pm, Severity.ERROR, e.getResourceData().msgKey, e.getResourceData().msgArgs);
      }
    }
  }

  protected void validateImpl(T pm)  throws PmValidationException {
    for (PmObject d : PmUtil.getPmChildren(pm)) {
      if (d.isPmVisible() && !d.isPmReadonly()) {
        d.pmValidate();
      }
    }
  }

}