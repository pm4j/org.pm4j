package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;

/**
 * Proxy commands are designed as stand-in's for optionally existing real application commands.
 * <p>
 * Command proxies do not directly trigger validations. That is the task of the {@link #delegateCmd} to delegate the
 * call to.
 *
 * @author olaf boede
 */
@PmCommandCfg(beforeDo=BEFORE_DO.DO_NOTHING)
public class PmCommandProxy extends PmCommandImpl {

    /**
     * Defines, what to do in case of a missing delegate command.
     */
    public static enum OnMissingDelegate {
        /** Shows the disabled command. */
        DISABLE,
        /** Makes the command invisible. */
        HIDE
    }

    /** The command to delegate the calls to. */
    private PmCommand delegateCmd;

    /** Defines what to do in case of a missing forward command. */
    private OnMissingDelegate onMissingDelegate;

    /** The clone of the delegate that has executed the last <code>doIt</code> call. */
    private PmCommand executedDelegateCmdClone;

    /**
     * @param pmParent The PM tree parent.
     * @param onMissingDelegate Defines what to do in case of a missing forward command.
     */
    public PmCommandProxy(PmObject pmParent, OnMissingDelegate onMissingDelegate) {
        super(pmParent);
        this.onMissingDelegate = onMissingDelegate;
    }

    /**
     * Defines a proxy that gets disabled if there is no forward command defined.
     *
     * @param pmParent The PM tree parent.
     */
    public PmCommandProxy(PmObject pmParent) {
        super(pmParent);
        this.onMissingDelegate = OnMissingDelegate.DISABLE;
    }

    /**
     * Defines the command that really executes the tool bar command functionality.
     *
     * @param delegateCmd
     *            The command that defines the logic.
     */
    public void setDelegateCmd(PmCommand delegateCmd) {
        this.delegateCmd = delegateCmd;
    }

    /**
     * @return The command that defines the logic. May be <code>null</code> if there is no delegate configured.
     */
    public PmCommand getDelegateCmd() {
      return delegateCmd;
    }

    @Override
    protected boolean isPmEnabledImpl() {
        return delegateCmd != null
                ? delegateCmd.isPmEnabled()
                : false;
    }

    @Override
    protected boolean isPmVisibleImpl() {
        return delegateCmd != null
                ? delegateCmd.isPmVisible()
                : onMissingDelegate != OnMissingDelegate.HIDE;
    }

    @Override
    protected void doItImpl() throws Exception {
      executedDelegateCmdClone = delegateCmd.doIt();
    }

    /**
     * Returns the clone of the delegate that has been executed.
     */
    @Override
    public PmCommand doIt(boolean changeCommandHistory) {
      PmCommandProxy proxyCmdClone = (PmCommandProxy) super.doIt(changeCommandHistory);
      return proxyCmdClone != null
                ? proxyCmdClone.executedDelegateCmdClone
                : executedDelegateCmdClone;
    }

    /**
     * Provides the state of the executed delegate command (if it already exists).
     */
    @Override
    public CommandState getCommandState() {
      return executedDelegateCmdClone != null
                ? executedDelegateCmdClone.getCommandState()
                : super.getCommandState();
    }

    @Override
    protected String getPmTitleImpl() {
      return delegateCmd != null
              ? delegateCmd.getPmTitle()
              : super.getPmTitleImpl();
    }

    @Override
    protected String getPmTooltipImpl() {
      return delegateCmd != null
              ? delegateCmd.getPmTooltip()
              : super.getPmTitleImpl();
    }
}