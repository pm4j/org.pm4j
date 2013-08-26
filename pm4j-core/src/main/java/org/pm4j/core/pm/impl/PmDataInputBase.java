package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.VisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.VisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.VisitResult;

public abstract class PmDataInputBase extends PmObjectBase implements PmDataInput {

  /**
   * An indicator that may be used to declare this PM as changed.
   */
  private boolean pmExpliciteChangedFlag;

  public PmDataInputBase(PmObject parentPm) {
    super(parentPm);
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

  protected boolean isPmValueChangedImpl() {
    if (pmExpliciteChangedFlag) {
      return true;
    }

    // XXX olaf: the tree related question should be factored out to a utiltiy.
    List<PmDataInput> items = PmUtil.getPmChildrenOfType(this, PmDataInput.class);
    for (int i = 0; i < items.size(); ++i) {
      PmDataInput d = items.get(i);

      if (PmInitApi.isPmInitialized(d) && // a not initialized PM can't have a change.
          d.isPmVisible() && !d.isPmReadonly() && // invisible and readonly too.
    	    (!(d instanceof PmConversation)) && // a sub-conversation does not influence the changed state
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
        VisitCallBack v = new VisitCallBack() {
          @Override
          public VisitResult visit(PmObject pm) {
            if (pm instanceof PmDataInputBase) {
              if (_setPmValueChangedForThisInstanceOnly((PmDataInputBase)pm, changed)) {
                childrenWithChangedStateChange.add(pm);
              }
            } else if (pm instanceof PmDataInput) {
              // If we find a different implementation we have to call the external interface.
              // XXX olaf: the children of this child may get duplicate calls when the visitor proceeds...
              ((PmDataInput) pm).setPmValueChanged(changed);
            }
            return VisitResult.CONTINUE;
          }
        };

        // If some of the by default skipped PMs should be traversed too: Please override setPmValueChangedImpl().
        PmVisitorApi.visitChildren(this, v,
            VisitHint.SKIP_NOT_INITIALIZED, // Not yet initialized PMs are not yet changed for sure.
            VisitHint.SKIP_CONVERSATION,    // Conversations have their own change handling.
            VisitHint.SKIP_INVISIBLE,       // Invisible parts should not be changed.
            VisitHint.SKIP_READ_ONLY);      // Read only parts should never be changed.
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
  private boolean _setPmValueChangedForThisInstanceOnly(PmDataInputBase pm, boolean newChangedState) {
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
  }

  /**
   * The default implementation validates the attributes.
   * <p>
   * Subclasses may override this to provide some more specific logic.
   * <p>
   * Important for overriding: Don't forget to call
   * <code>super.pmValidate()</code> to ensure attribute validation.
   */
  @Override
  public void pmValidate() {
    if (isPmVisible() && !isPmReadonly()) {
      for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
        if (d.isPmVisible() && !d.isPmReadonly()) {
          d.pmValidate();
        }
      }
    }
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
