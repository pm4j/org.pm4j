package org.pm4j.core.pm.impl;

import java.util.List;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmObject;

public abstract class PmDataInputBase extends PmObjectBase implements PmDataInput {

  public PmDataInputBase(PmObject parentPm) {
    super(parentPm);
  }

  @Override
  public boolean isPmValueChanged() {
    // TODO olaf: add caching (and event support to update the cache state).
    List<PmDataInput> items = zz_getPmDataInputPms();
    for (int i = 0; i < items.size(); ++i) {
      PmDataInput d = items.get(i);
      if (d.isPmValueChanged() && d.isPmVisible()) {
        return true;
      }
    }
    // No changes found:
    return false;
  }

  @Override
  public void resetPmValues() {
    for (PmDataInput d : zz_getPmDataInputPms()) {
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
      for (PmDataInput d : zz_getPmDataInputPms()) {
        if (d.isPmVisible() && !d.isPmReadonly()) {
          if (d instanceof PmAttr<?>) {
            // XXX olaf: duplicate validation may occur in case of duplicate command calls
            // (some Ajax calls do that.)
            // Find a clean way to prevent that.

            if (d.isPmValid()) {
              d.pmValidate();
            }
          }
          else {
            d.pmValidate();
          }
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
    for (PmDataInput d : zz_getPmDataInputPms()) {
      d.rollbackBufferedPmChanges();
    }
  }

  @Override
  public void commitBufferedPmChanges() {
    for (PmDataInput d : zz_getPmDataInputPms()) {
      d.commitBufferedPmChanges();
    }
  }

}
