package org.pm4j.core.pm.impl;

import java.util.List;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;

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
  public final void setPmValueChanged(boolean changed) {
    boolean wasChanged = isPmValueChanged();
    if (wasChanged != changed) {
      setPmValueChangedImpl(changed);
      PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGED_STATE_CHANGE);
    }
  }

  protected void setPmValueChangedImpl(boolean changed) {
    pmExpliciteChangedFlag = changed;
    // XXX olaf: use a visitor...
    if (changed == false && pmInitState == PmInitState.INITIALIZED) {
      List<PmDataInput> items = PmUtil.getPmChildrenOfType(this, PmDataInput.class);
      for (int i = 0; i < items.size(); ++i) {
        PmDataInput d = items.get(i);
        d.setPmValueChanged(changed);
      }
    }
    // TODO olaf: fire a changed state change event.
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
