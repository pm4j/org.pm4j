package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTabSet;
import org.pm4j.core.pm.impl.connector.PmTabSetConnector;

/**
 * Basic implementation of a {@link PmTabSet}.
 *
 * @author olaf boede
 */
public class PmTabSetImpl extends PmElementImpl implements PmTabSet {

  /** A view technology specific tab set logic connector. */
  private PmTabSetConnector pmToTabSetViewConnector;

  /** The set of tab switch command decorator definitions. */
  private List<CmdDecoratorDefintion> pmCmdDecoratorDefinitions = new ArrayList<CmdDecoratorDefintion>();

  /**
   * Default constructor (for some dependency injection scenarios).<br>
   * {@link #setPmParent(PmObject)} needs to be called before the instance can be used.
   */
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

  /**
   * Adds a {@link PmCommandDecorator} to be executed before a tab switch.
   *
   * @param fromTab The from-tab to define the decorator for. If it is <code>null</code>, the decorator will be active for all from-tabs.
   * @param toTab The to-tab to define the decorator for. If it is <code>null</code>, the decorator will be active for all to-tabs.
   * @param decorator The decorator logic to be executed before the tab switch.
   */
  public void addTabSwitchCommandDecorator(PmElement fromTab, PmElement toTab, PmCommandDecorator decorator) {
    pmCmdDecoratorDefinitions.add(new CmdDecoratorDefintion(fromTab, toTab, decorator));
  }

  @Override
  public boolean switchToTabPm(PmElement fromTab, PmElement toTab) {
    // Delegate to an undoable command.
    PmTabChangeCommand tabChangeCommand = new PmTabChangeCommand(this, fromTab, toTab);
    for (CmdDecoratorDefintion d : pmCmdDecoratorDefinitions) {
      if (d.isDecoratorForSwitch(fromTab, toTab)) {
        tabChangeCommand.addCommandDecorator(d.getDecorator());
      }
    }

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

  /**
   * @return A view technology specific tab set logic connector.
   */
  public PmTabSetConnector getPmToTabSetViewConnector() {
    zz_ensurePmInitialization();
    return pmToTabSetViewConnector;
  }

  /**
   * Container for a tab navigation specific command decorator definition.
   */
  class CmdDecoratorDefintion {
    private String fromName;
    private String toName;
    private PmCommandDecorator decorator;

    public CmdDecoratorDefintion(PmObject fromTab, PmObject toTab, PmCommandDecorator decorator) {
      this(fromTab != null ? fromTab.getPmRelativeName() : null,
           toTab != null ? toTab.getPmRelativeName() : null,
           decorator);
    }
    public CmdDecoratorDefintion(String fromTabName, String toTabName, PmCommandDecorator decorator) {
      this.fromName = fromTabName;
      this.toName = toTabName;
      this.decorator = decorator;
    }

    public boolean isDecoratorForSwitch(PmObject fromTab, PmObject toTab) {
      return (toTab == null || toTab.getPmRelativeName().equals(toName)) &&
             (fromTab == null || fromTab.getPmRelativeName().equals(fromName));
    }

    public PmCommandDecorator getDecorator() { return decorator; }
  }
}
