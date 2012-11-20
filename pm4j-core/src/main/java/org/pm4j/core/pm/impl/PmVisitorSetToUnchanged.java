package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;

/**
 * Sets all tree items of PM to an unchanged state.
 */
public class PmVisitorSetToUnchanged extends PmVisitorAdapter {

  @Override
  protected void onVisit(PmObject pm) {
    super.onVisit(pm);
    if (pm instanceof PmDataInput) {
      ((PmDataInput)pm).setPmValueChanged(false);
    }
  }

  @Override
  public void visit(@SuppressWarnings("rawtypes") PmTable table) {
    for (Object r : table.getRowsWithChanges()) {
      if (r instanceof PmObject) {
        ((PmObject)r).accept(this);
      }
    }
    super.visit(table);
  }

  @Override
  public void visit(PmElement element) {
    for (PmObject p : PmUtil.getPmChildren(element)) {
      if (PmInitApi.isPmInitialized(p) && p.isPmVisible() && !p.isPmReadonly() && (!(p instanceof PmConversation))) {
        p.accept(this);
      }
    }
    super.visit(element);
  }

}
