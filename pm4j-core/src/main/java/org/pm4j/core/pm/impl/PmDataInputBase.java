package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;

public abstract class PmDataInputBase extends PmObjectBase implements PmDataInput {

  /**
   * An indicator that may be used to declare this PM as changed.
   */
  private boolean pmExpliciteChangedFlag;

  public PmDataInputBase(PmObject parentPm) {
    super(parentPm);

    // load/reload new data leads to an unchanged state.
    PmEventApi.addPmEventListener(this, PmEvent.VALUE_CHANGE, new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        if (event.isInitializationEvent() || event.isReloadEvent()) {
          onPmDataExchangeEvent(event);
        }
      }
    });
  }

  /**
   * This method gets called whenever the PM observes an initialization or
   * reload event. The default implementation resets the changed state for this
   * PM only. It does not
   * <code>setPmValueChanged(false)</code>.
   * <p>
   * You may change that by overriding this method.
   * <p>
   * This method supports the common use case of a PM that represents a some
   * content of a data bean. The bean is then usually the backing bean of one of
   * the PM parents. If that bean gets exchanged, a value change event will be
   * propagated to the related PM sub-tree. See:
   * {@link PmBean#setPmBean(Object)}.
   *
   * @param parentEvent
   *          The event that was received by the parent.
   */
  protected void onPmDataExchangeEvent(PmEvent parentEvent) {
    // This kind event gets recursively applied to a PM tree (part). Because of that we don't need to
    // handle the child PMs.
    _setPmValueChangedForThisInstanceOnly(this, false);
  }


  /**
   * Implements the fix framework behavior.<br>
   * Please override {@link #isPmValueChangedImpl()} to implement a customized logic.
   */
  @Override
  public final boolean isPmValueChanged() {
    // Extension point for changed state caching.

    // A not yet initialized PM is always not yet changed.
    return (pmInitState == PmInitState.INITIALIZED) &&
           isPmValueChangedImpl();
  }

  /**
   * Returns <code>true</code> if this PM or one of its child PMs reports value change.
   * Several implementations such as {@link PmAttrBase} provide basic mechanisms to observe
   * user changes.
   * <p>
   * If the changed state of your PM is not relevant to report changes, you may override this method
   * and return simply <code>false</code>.
   * This may be the case for fields that just control a view state, like a display language field
   * that never gets stored.
   * <p>
   * If you need to influence that state manually, you may use {@link #setPmValueChanged(boolean)}.
   * Please do that carefully, because you overrule the existing mechanisms.
   *
   * @return <code>true</code> if the PM or one of it's sub-PMs reports a relevant value change.
   */
  protected boolean isPmValueChangedImpl() {
    // If a programmer has marked this instance explicitly as changed by calling
    // setPmValueChanged(true), this will be considered first.
    if (pmExpliciteChangedFlag) {
      return true;
    }

    // Ask all children if at least one of them reports a value change.
    List<PmDataInput> items = PmUtil.getPmChildrenOfType(this, PmDataInput.class);
    for (int i = 0; i < items.size(); ++i) {
      PmDataInput d = items.get(i);

      if (PmInitApi.isPmInitialized(d) && // a not initialized PM can't have a change.
          !d.isPmReadonly() && // readonly too.
          (!(d instanceof PmConversation)) && // a sub-conversation does not influence the changed state.
          d.isPmValueChanged()) {
        return true;
      }
    }
    // No changes found:
    return false;
  }

  @Override
  public final void setPmValueChanged(final boolean changed) {
    boolean changedStateChanged = _setPmValueChangedForThisInstanceOnly(this, changed);

    if (changedStateChanged) {
      // Inform about the change directly:
      PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGE);

      // Collects the state change information to send the related event only when the
      // state change of the related sub-tree is really completed.
      final List<PmObject> childrenWithChangedStateChange = new ArrayList<PmObject>();

      // Only if the changed flag was set to 'false': Reset the changed states for all sub-PMs.
      if ((changed == false) && (pmInitState == PmInitState.INITIALIZED)) {
        PmVisitCallBack v = new PmVisitCallBack() {
          @Override
          public PmVisitResult visit(PmObject pm) {
            if (pm instanceof PmDataInputBase) {
              if (_setPmValueChangedForThisInstanceOnly((PmDataInputBase)pm, changed)) {
                childrenWithChangedStateChange.add(pm);
              }
            } else if (pm instanceof PmDataInput) {
              // If we find a different implementation we have to call the external interface.
              // XXX olaf: the children of this child may get duplicate calls when the visitor proceeds...
              ((PmDataInput) pm).setPmValueChanged(changed);
            }
            return PmVisitResult.CONTINUE;
          }
        };

        // If some of the by default skipped PMs should be traversed too: Please override setPmValueChangedImpl().
        PmVisitorApi.visitChildren(this, v,
            PmVisitHint.SKIP_NOT_INITIALIZED, // Not yet initialized PMs are not yet changed for sure.
            PmVisitHint.SKIP_CONVERSATION,    // Conversations have their own change handling.
            PmVisitHint.SKIP_INVISIBLE,       // Invisible parts should not be changed.
            PmVisitHint.SKIP_READ_ONLY);      // Read only parts should never be changed.
      }

      // Inform about changed state changes for all children and this instance.
      for (PmObject childPm : childrenWithChangedStateChange) {
        PmEventApi.firePmEvent(childPm, PmEvent.VALUE_CHANGED_STATE_CHANGE);
      }
      PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGED_STATE_CHANGE);
    }
  }

  /**
   * Internal change logic extension point.
   *
   * @param changed the new explicitely assinged changed state.
   */
  protected void setPmValueChangedImpl(boolean changed) {
  }

  /**
   * Internal helper that adjusts the changed state for this PM only.
   * @param pm
   * @param newChangedState
   * @return <code>true</code> if the changed state of the PM was changed by this call.
   */
  boolean _setPmValueChangedForThisInstanceOnly(PmDataInputBase pm, boolean newChangedState) {
    boolean wasChanged = pm.isPmValueChanged();

    pm.pmExpliciteChangedFlag = newChangedState;
    pm.setPmValueChangedImpl(newChangedState);

    return wasChanged != newChangedState;
  }

  @Override
  public void resetPmValues() {
    for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
      d.resetPmValues();
    }
    setPmValueChanged(false);
  }

  @Override
  protected Validator makePmValidator() {
    return getPmMetaDataWithoutPmInitCall().deprValidation
        ? new DeprDataInputValidator<PmDataInputBase>()
        : new ObjectValidator<PmDataInputBase>();
  }

  // ======== Buffered data input support ======== //

  @Override
  public boolean isBufferedPmValueMode() {
    return getPmConversation().isBufferedPmValueMode();
  }

  @Override
  public void rollbackBufferedPmChanges() {
    for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
      d.rollbackBufferedPmChanges();
    }
  }

  @Override
  public void commitBufferedPmChanges() {
    for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
      d.commitBufferedPmChanges();
    }
  }

}
