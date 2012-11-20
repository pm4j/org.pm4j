package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;

/**
 * Calls {@link PmObject#firePmEvent(PmEvent)} for all visited
 * child PMs.
 */
public class PmVisitorFireEvent extends PmVisitorAdapter {

  private PmEvent event;

  public PmVisitorFireEvent(PmEvent event) {
    this.event = event;
  }

  @Override
  protected void onVisit(PmObject pm) {
    PmEventApi.firePmEvent(pm, event);
    for (PmObject child : PmUtil.getPmChildren(pm)) {
      // Switch for each child to an event that is related to the child.
      // Registered listeners for the child will expect that the event contains a
      // reference to the PM it was registered for.
      PmVisitorFireEvent childVisitor = new PmVisitorFireEvent(new PmEvent(event.getSource(), child, event.getChangeMask(), event.getValueChangeKind()));
      child.accept(childVisitor);
    }
  }
}
