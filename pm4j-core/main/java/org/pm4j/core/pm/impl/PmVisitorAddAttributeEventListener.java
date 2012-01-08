package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;

/**
 * Adds an event listener to all attributes within a PM tree structure.
 */
public class PmVisitorAddAttributeEventListener extends PmVisitorAdapter {

  private final PmEventListener listener;
  private final int changeMask;

  public PmVisitorAddAttributeEventListener(int changeMask, PmEventListener listener) {
    this.listener = listener;
    this.changeMask = changeMask;
  }

  @Override
  public void visit(PmAttr<?> attr) {
    super.visit(attr);
    PmEventApi.addPmEventListener(attr, changeMask, listener);
  }

  @Override
  protected void onVisit(PmObject pm) {
    for (PmObject child : PmUtil.getPmChildren(pm)) {
      child.accept(this);
    }
  }
}
