package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventApi;

/**
 * Implements the validation logic that was formerly in {@link PmAttrBase#pmValidate()}.
 */
class DeprAttrValidator<T_VALUE> extends DeprDataInputValidator<PmAttrBase<T_VALUE, ?>> {

  @Override
  protected void validateImpl(PmAttrBase<T_VALUE, ?> pm) throws PmValidationException {
    // visibility is considered here to support attribute exchange via visiblity switch.
    if (pm.isPmVisible() &&
        pm.isPmEnabled()) {
      // A validation can only be performed if the last setValue() did not generate a converter exception.
      // Otherwise the attribute will simply stay in its value converter error state.
      if (!pm.hasPmConverterErrors()) {
        boolean wasValid = pm.isPmValid();

        PmConversationImpl conversation = pm.getPmConversationImpl();
        conversation.clearPmMessages(pm, null);
        try {
          pm.validate(pm.getValue());
          pm.performJsr303Validations();
        }
        catch (PmValidationException e) {
          PmResourceData exResData = e.getResourceData();
          conversation.addPmMessage(new PmValidationMessage(pm, exResData.msgKey, exResData.msgArgs));
        }
        catch (RuntimeException e) {
          conversation.getPmExceptionHandler().onExceptionInPmValidation(pm, e);
        }

        boolean isValid = pm.isPmValid();
        if (isValid != wasValid) {
          PmEventApi.firePmEvent(pm, PmEvent.VALIDATION_STATE_CHANGE);
        }
      }
    }
  }
}