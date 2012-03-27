package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTabSet;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.impl.connector.PmTabSetConnector;

/**
 * Basic implementation of a {@link PmTabSet}.
 *
 * @author olaf boede
 */
public class PmTabSetImpl extends PmElementImpl implements PmTabSet {

  private static final Log LOG = LogFactory.getLog(PmTabSetImpl.class);

  /** A view technology specific tab set logic connector. */
  private PmTabSetConnector pmToTabSetViewConnector;

  /** The set of tab switch command decorator definitions. */
  private List<CmdDecoratorDefintion> pmCmdDecoratorDefinitions = new ArrayList<CmdDecoratorDefintion>();

  /** PM of the currently active tab. */
  private PmElement currentTabPm;

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
  public boolean switchToTabPm(PmElement toTab) {
    return switchToTabPm(getCurrentTabPm(), toTab);
  }

  @Override
  public boolean switchToTabPm(PmElement fromTab, PmElement toTab) {
    PmElement _fromTab = fromTab != null ? fromTab : getCurrentTabPm();

    // ensure that the to-tab is initialized (was an issue in domain specific unit tests):
    toTab.getPmTooltip();

    // Delegate to an undoable command.
    PmTabChangeCommand tabChangeCommand = new PmTabChangeCommand(this, _fromTab, toTab);
    for (CmdDecoratorDefintion d : pmCmdDecoratorDefinitions) {
      if (d.isDecoratorForSwitch(_fromTab, toTab)) {
        tabChangeCommand.addCommandDecorator(d.getDecorator());
      }
    }

    PmTabChangeCommand executedCommand = (PmTabChangeCommand) tabChangeCommand.doIt();

    if (executedCommand.getCommandState() == CommandState.EXECUTED) {
      currentTabPm = toTab;
    }
    else if (LOG.isDebugEnabled()) {
      String msg = "The UI logic prevented a switch from tab " + PmUtil.getPmLogString(_fromTab) + " to " +
          PmUtil.getPmLogString(toTab) + ".";

      if (executedCommand.getVetoCommandDecorator() != null) {
        msg += " It has been prevented by the command decorator: " + executedCommand.getVetoCommandDecorator();
      }
      LOG.debug(msg);
    }

    // If the UI logic prevents the tab navigation, no exception will be thrown.
    // So we check here if the tab navigation was really successfully performed.
    return executedCommand.getCommandState() == CommandState.EXECUTED;
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
   * @return The currently active tab.
   */
  @Override
  public PmElement getCurrentTabPm() {
    return (currentTabPm != null)
            ? currentTabPm
            : getFirstTabPm();
  }

  /**
   * The default implementation just provides the set of all sub-elements.
   * <p>
   * Please override the implementation if that default behavior does not match.
   *
   * @return The set of tabs.
   */
  @Override
  public List<PmElement> getTabPms() {
    return zz_getPmElements();
  }

  /**
   * Provides the first tab within the tab set.
   * <p>
   * The default implementation just provides the first sub-element.<br>
   * It should be overridden if the first tab is not the first sub-element.
   *
   * @return The first tab of the tab set.
   */
  protected PmElement getFirstTabPm() {
    List<PmElement> subElements = getTabPms();
    if (subElements.size() > 0) {
      return subElements.get(0);
    }
    else {
      LOG.warn("Tabset '" + getPmRelativeName() + "' has no sub tab PMs.");
      return null;
    }
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
    private final PmElement fromTab;
    private final PmElement toTab;

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
      // Only successfully executed tab switches need to be undone.
      setUndoCommand(new PmTabChangeCommand(tabSet, toTab, fromTab));
    }

  }

}
