package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTabSet;
import org.pm4j.core.pm.impl.connector.PmTabSetConnector;

public class PmTabSetImpl extends PmElementImpl implements PmTabSet {

  private PmTabSetConnector pmToTabSetViewConnector;


  public PmTabSetImpl() {
    super();
  }

  public PmTabSetImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  protected void onPmInit() {
    super.onPmInit();
    pmToTabSetViewConnector = getPmConversationImpl().getViewConnector().createTabSetConnector(this);

    assert pmToTabSetViewConnector != null;
  }

  @Override
  public boolean switchToTabPm(PmElement fromTab, PmElement toTab) {
    // Delegate to an undoable command.
    PmTabChangeCommand tabChangeCommand = new PmTabChangeCommand(this, fromTab, toTab);
    PmTabChangeCommand executedCommand = (PmTabChangeCommand) tabChangeCommand.doIt();

    // If the UI logic prevents the tab navigation, no exception will be thrown.
    // So we check here if the tab navigation was really successfully performed.
    return executedCommand.isExecuted();
  }

  /**
   * Subclasses may define here their specific UI logic here.
   * <p>
   * The default implementation always allows to switch the tab.
   * <p>
   * Internally this method gets called by the {@link PmTabChangeCommand}.
   */
  protected boolean switchToTabPmImpl(PmElement fromTab, PmElement toTab) {
    return true;
  }

  public PmTabSetConnector getPmToTabSetViewConnector() {
    zz_ensurePmInitialization();
    return pmToTabSetViewConnector;
  }

}
