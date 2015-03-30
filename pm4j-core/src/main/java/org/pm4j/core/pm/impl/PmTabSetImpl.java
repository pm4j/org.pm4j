package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.PmTabSet;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.impl.connector.PmTabSetConnector;
import org.pm4j.navi.NaviLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a {@link PmTabSet}.
 * <p>
 * Each child PM having the marker interface {@link PmTab} will be considered as a tab of this set.
 * <p>
 * The tabs are sorted as they are declared.
 *
 * @author Olaf Boede
 */
public class PmTabSetImpl extends PmObjectBase implements PmTabSet {

  private static final Logger LOG = LoggerFactory.getLogger(PmTabSetImpl.class);

  /** The set of tab switch command decorator definitions. */
  private List<TabSwitchDecoratorDefintion> pmCmdDecoratorDefinitions = new ArrayList<TabSwitchDecoratorDefintion>();

  /** PM of the currently active tab. */
  private PmTab currentTabPm;
  /** Reference to tab that was formerly the current one but was de-selected by a {@link #resetCurrentTabPmIfInactive()} call. */
  private PmTab resettedCurrentTabPm;

  /**
   * Creates the tab set PM.
   *
   * @param pmParent the PM hierarchy parent instance.
   */
  public PmTabSetImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  protected void onPmDataExchangeEvent(PmEvent parentEvent) {
    super.onPmDataExchangeEvent(parentEvent);
    resetCurrentTabPmIfInactive();
  }

  /**
   * If the currentTabPm is not visible or not enabled the internal {@link #currentTabPm} reference
   * will be reset to <code>null</code>. The next call to {@link #getCurrentTabPm()} will re-calculate
   * the current tab using the logic provided by {@link #getFirstTabPm()}.
   */
  public void resetCurrentTabPmIfInactive() {
    if (currentTabPm != null && !isTabActive(currentTabPm)) {
      resettedCurrentTabPm = currentTabPm;
      currentTabPm = null;
    }
  }

  /**
   * Defines if <code>tab</code> may be opened.<br>
   * This can be only be done it the tab is visible and enabled.
   *
   * @param tab
   *          to be tested
   * @return <code>true</code>, if the tab may be opened.
   */
  protected boolean isTabActive(PmTab tab) {
    return tab.isPmVisible() && tab.isPmEnabled();
  }

  /**
   * Adds a {@link PmCommandDecorator} to be executed before a tab switch.
   * <p>
   * Alternatively you may override {@link #switchToTabPmImpl(PmCommand, PmTab, PmTab)}.
   *
   * @param fromTab The from-tab to define the decorator for. If it is <code>null</code>, the decorator will be active for all from-tabs.
   * @param toTab The to-tab to define the decorator for. If it is <code>null</code>, the decorator will be active for all to-tabs.
   * @param decorator The decorator logic to be executed before the tab switch.
   */
  public void addTabSwitchCommandDecorator(PmTab fromTab, PmTab toTab, PmCommandDecorator decorator) {
    pmCmdDecoratorDefinitions.add(new TabSwitchDecoratorDefintion(fromTab, toTab, decorator));
  }

  @Override
  public boolean switchToTabPm(PmTab toTab) {
    PmTab _fromTab = currentTabPm != null ? currentTabPm : getFirstTabPm();
    return _switchToTabPm(_fromTab, toTab);
  }

  private boolean _switchToTabPm(PmTab _fromTab, PmTab toTab) {
    if (_fromTab == toTab) {
      // nothing to do. 'successfully' done.
      return true;
    }

    // ensure that the to-tab is initialized (was an issue in domain specific
    // unit tests):
    PmInitApi.initPmTree(toTab);

    // Delegate to an undoable command.
    PmTabChangeCommand tabChangeCommand = new PmTabChangeCommand(this, _fromTab, toTab);
    for (TabSwitchDecoratorDefintion d : pmCmdDecoratorDefinitions) {
      if (d.isDecoratorForSwitch(_fromTab, toTab)) {
        tabChangeCommand.addCommandDecorator(d.getDecorator());
      }
    }

    PmTabChangeCommand executedCommand = (PmTabChangeCommand) tabChangeCommand.doIt();

    if (LOG.isDebugEnabled() && executedCommand.getCommandState() != CommandState.EXECUTED) {
      String msg = "The UI logic prevented a switch from tab " + PmUtil.getPmLogString(_fromTab) + " to "
          + PmUtil.getPmLogString(toTab) + ".";

      if (executedCommand.getVetoCommandDecorator() != null) {
        msg += " It has been prevented by the command decorator: " + executedCommand.getVetoCommandDecorator();
      }
      LOG.debug(msg);
    }

    // If the UI logic prevents the tab navigation, no exception will be thrown.
    // So we check here if the tab navigation was really successfully performed.
    if (executedCommand.getCommandState() == CommandState.EXECUTED) {
      // The visible tab needs to be loaded/initialized.
      BroadcastPmEventProcessor.doDeferredEventsForVisiblePms(toTab);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets called before a tab switch operation. Subclasses may control here if
   * the tab switch should be allowed.<br>
   * The default implementation always allows to switch the tab.
   * <p>
   * For more generic logic you may consider using
   * {@link #addTabSwitchCommandDecorator(PmTab, PmTab, PmCommandDecorator)}.
   *
   * @param fromTab
   *          The tab to leave.
   * @param toTab
   *          The tab to enter.
   * @param tabChangeCmd
   *          The internally used tab change command. May be used for command confirmation scenarios.
   *
   * @return <code>true</code> if the switch is allowed.<br>
   *         <code>false</code> prevents the tab switch.
   *
   */
  protected boolean beforeSwitch(PmTab fromTab, PmTab toTab, PmCommand tabChangeCmd) {
    return true;
  }

  /**
   * Gets called after a successful tab switch operation.
   *
   * @param fromTab
   *          The tab that was left.
   * @param toTab
   *          The tab new current tab.
   */
  protected void afterSwitch(PmTab fromTab, PmTab toTab) {
  }

  /**
   * Provides the currently active tab.
   * <p>
   * The initial call uses {@link #getFirstTabPm()} to evaluate the tab to open
   * initially.
   * <p>
   * In case of an automatic tab-switch caused by
   * {@link #resetCurrentTabPmIfInactive()} we have to notify the registered tab
   * switch decorators. We can't call <code>beforeDo()</code> because we can't
   * handle vetos. But we can call <code>afterDo()</code> here to make clear
   * that a switch happened.
   *
   * @return The currently active tab.
   */
  @Override
  public PmTab getCurrentTabPm() {
    PmInitApi.initThisPmOnly(this);
    if (currentTabPm == null) {
      currentTabPm = getFirstTabPm();
      if (resettedCurrentTabPm != null) {
        if (!_switchToTabPm(resettedCurrentTabPm, currentTabPm)) {
          currentTabPm = null;
          throw new PmRuntimeException(this, "Unable to leave the inactive tab '" +
                      resettedCurrentTabPm.getPmName() +
                      ". Please check your beforeDo veto logic. It should be less restrictive for this case.");
        }
        resettedCurrentTabPm = null;
      }
    }

    return currentTabPm;
  }

  /**
   * The default implementation just provides the set of all sub-elements.
   * <p>
   * Please override the implementation if that default behavior does not match.
   *
   * @return The set of tabs.
   */
  @Override
  public List<PmTab> getTabPms() {
    return PmUtil.getPmChildrenOfType(this, PmTab.class);
  }

  @Override
  public int getTabIndex(PmTab pmTab) {
    List<PmTab> list = getTabPms();
    for (int i=0; i<list.size(); ++i) {
      PmTab t = list.get(i);
      if (t.equals(pmTab)) {
        return i;
      }
    }

    // not found
    throw new PmRuntimeException(this, "The given tab does not belong to the tab set: " + pmTab);
  }

  /**
   * Provides the first tab within the tab set.
   * <p>
   * The default implementation just provides the first sub-element.<br>
   * It should be overridden if the first tab is not the first sub-element.
   *
   * @return The first tab of the tab set. Never <code>null</code>.
   */
  protected PmTab getFirstTabPm() {
    List<PmTab> tabs = getTabPms();
    if (tabs == null || tabs.isEmpty()) {
      throw new PmRuntimeException(this, "Tab set without tabs can't be used.");
    }

    for (PmTab t : tabs) {
      if (isTabActive(t)) {
        return t;
      }
    }

    LOG.warn("Tabset '" + getPmRelativeName() + "' has no sub active tab PMs. The first inactive tab gets the current one.");
    return getTabPms().get(0);
  }

  /**
   * @return A view technology specific tab set logic connector.
   */
  private PmTabSetConnector getPmToTabSetViewConnector() {
    return (PmTabSetConnector) getPmToViewConnector();
  }

  /**
   * Container for a tab switch specific command decorator definition.
   */
  class TabSwitchDecoratorDefintion {
    private String fromName;
    private String toName;
    private PmCommandDecorator decorator;

    public TabSwitchDecoratorDefintion(PmTab fromTab, PmTab toTab, PmCommandDecorator decorator) {
      this(fromTab != null ? fromTab.getPmRelativeName() : null,
           toTab != null ? toTab.getPmRelativeName() : null,
           decorator);
    }

    public TabSwitchDecoratorDefintion(String fromTabName, String toTabName, PmCommandDecorator decorator) {
      this.fromName = fromTabName;
      this.toName = toTabName;
      this.decorator = decorator;
    }

    public boolean isDecoratorForSwitch(PmObject fromTab, PmObject toTab) {
      return ((toName == null) ||
              (toTab != null && toTab.getPmRelativeName().equals(toName))) &&
             ((fromName == null) ||
              (fromTab != null) && fromTab.getPmRelativeName().equals(fromName));
    }

    public PmCommandDecorator getDecorator() { return decorator; }
  }

  /**
   * The command that internally executes a tab switch.
   * <p>
   * It supports undo and command decorators.
   */
  @PmCommandCfg(beforeDo=BEFORE_DO.DO_NOTHING)
  static class PmTabChangeCommand extends PmCommandImpl {

    private final PmTabSetImpl tabSet;
    private final PmTab fromTab;
    private final PmTab toTab;

    public PmTabChangeCommand(PmTabSetImpl tabSet, PmTab fromTab, PmTab toTab) {
      super(toTab);

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
        canDo = tabSet.beforeSwitch(fromTab, toTab, this);
      }
      return canDo;
    }

    @Override
    protected void doItImpl() {
      PmTabSetConnector viewConnector = tabSet.getPmToTabSetViewConnector();
      if (viewConnector != null) {
        viewConnector.switchToTab(toTab);
      }
      // Only successfully executed tab switches need to be undone.
      setUndoCommand(new PmTabChangeCommand(tabSet, toTab, fromTab));
    }

    @Override
    protected NaviLink afterDo(boolean changeCommandHistory) {
      tabSet.currentTabPm = toTab;
      tabSet.afterSwitch(fromTab, toTab);
      return super.afterDo(changeCommandHistory);
    }
  }

  /**
   * Provides a possibility to react on tab changes together with the information about the source and target tab.
   *
   * @author MMANZ
   */
  public static class PmTabSetCommandDecoratorAdapter implements PmCommandDecorator {

    @Override
    public final boolean beforeDo(PmCommand cmd) {
      PmTabChangeCommand tabChangeCommand = (PmTabChangeCommand) cmd;
      return beforeTabChange(tabChangeCommand.fromTab, tabChangeCommand.toTab);
    }

    /* (non-Javadoc)
     * @see org.pm4j.core.pm.PmCommandDecorator#afterDo(org.pm4j.core.pm.PmCommand)
     */
    @Override
    public final void afterDo(PmCommand cmd) {
      PmTabChangeCommand tabChangeCommand = (PmTabChangeCommand) cmd;
      afterTabChange(tabChangeCommand.fromTab, tabChangeCommand.toTab);
    }

    protected void afterTabChange(PmTab fromTab, PmTab toTab) {
      // do nothing
    }

    protected boolean beforeTabChange(PmTab fromTab, PmTab toTab) {
      return true;
    }
  }


}
