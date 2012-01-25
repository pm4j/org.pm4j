package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmElement;

public class PmTabChangeCommand extends PmCommandImpl {

  private final PmTabSetImpl tabSet;
  private final PmElement fromTab;
  private final PmElement toTab;
  // XXX olaf: move an execution state enum to base implementation.
  private boolean executed;

  public PmTabChangeCommand(PmTabSetImpl tabSet, PmElement fromTab, PmElement toTab) {
    super((fromTab != null)
        ? fromTab
        : tabSet);

    this.tabSet = tabSet;
    this.fromTab = fromTab;
    this.toTab = toTab;

    assert fromTab != null;
    assert toTab != null;
  }

  /**
   * The tab switch can only be executed if the usual preconditions are fulfilled
   * and the PmTab
   */
  @Override
  protected boolean beforeDo() {
    boolean canDo = super.beforeDo();
    if (canDo) {
      canDo = tabSet.switchToTabPmImpl(fromTab, toTab);
    }
    return canDo;
  }

  @Override
  protected void doItImpl() {
    tabSet.getPmToTabSetViewConnector()._switchToTab(toTab);
    executed = true;
    // Only successfully executed tab switches need to be undone.
    setUndoCommand(new PmTabChangeCommand(tabSet, toTab, fromTab));
  }

  protected boolean isExecuted() {
    return executed;
  }

}
