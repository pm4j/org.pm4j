package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;

/**
 * Sets all tree items of PM to an unchanged state.
 */
public class PmVisitorSetToUnchanged extends PmVisitorAdapter {

  @Override
  public void visit(PmAttr<?> attr) {
    attr.setPmValueChanged(false);
  }

  @Override
  public void visit(PmElement element) {
    for (PmObject p : PmUtil.getPmChildren(element)) {
      if (! p.isPmReadonly()) {
        p.accept(this);
      }
    }
  }

}
