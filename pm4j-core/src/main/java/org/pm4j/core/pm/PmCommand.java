package org.pm4j.core.pm;

import java.util.List;

import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.navi.NaviLink;



/**
 * A user interface command.
 *
 * @author olaf boede
 */
public interface PmCommand extends PmObject {

  public enum CmdKind {
    /**
     * Commands that are in general executeable.
     * <p>
     * An executeable command may also have subcommands provided by
     * ({@link PmCommand#getSubCommandList()}).
     * This way an executeable command may also act as command group.
     */
    COMMAND,

    /**
     * A pure organizational structure of commands.
     */
    GROUP,

    /**
     * A separator between command sections of a command list.
     */
    SEPARATOR
  };

  public static enum CommandState {
    /** The command is just a template for command clones to be executed. */
    TEMPLATE,
    /** The initial state of a command execution clone instance. */
    CLONED,
    /** The <code>doItImpl</code> method was not executed because the <code>beforeDo</code> logic returned <code>false</code>. */
    BEFORE_DO_RETURNED_FALSE,
    /** The <code>doItImpl</code> method of the command was successfully executed. */
    EXECUTED,
    /** The execution of the command failed with an exception. */
    FAILED
  }

  public static enum CommandSet { ALL, POPUP, MENU_BAR, TOOL_BAR, BUTTONS };

  /**
   * Does the operation.
   * <p>
   * It internally clones this command instance and perfroms the operation using
   * the cloned command.<br>
   * This prototype pattern approach allows to store execution time specific
   * values within the command (E.g. navigation links, values, an undo-command).
   * <p>
   * Returns the command instance (clone) that performed the operation.
   * <p>
   * Any error messages are reported as error messages.
   *
   * @return The command clone that performed the operation.
   */
  PmCommand doIt();

  /**
   * Provides an instance that may undo the effect of this command.
   * <p>
   * A command is 'undo-able' when this method returns <code>null</code>.
   *
   * @return The command that can undo the effect of this command or
   *         <code>null</code>.
   */
  PmCommand getUndoCommand();

  /**
   * Interface for web frameworks like JSF.
   *
   * @return Implementation dependent. Typically "myPm.cmdDoSomeWork.success" or "myPm.cmdDoSomeWork.failed".
   */
  String doItReturnString();

  /**
   * Command call with void return type. Used to support some ui frameworks (such as a4j)
   * that need void signatures for actions.
   */
  void doItReturnVoid();

  /**
   * @return The {@link CmdKind} of this command item.
   */
  CmdKind getCmdKind();

  /**
   * A command may be nested in a hierarchy of other parent commands (command
   * groups). This method returns this set. The first list item is the root
   * group. The last list item is the nearest parent.
   *
   * @return The parent command list. In case of no parents an empty list, never
   *         <code>null</code>.
   */
  List<PmCommand> getParentCommands();

  /**
   * Provides an optional link to a navigation target.
   * <p>
   * Is usually provided by commands that just perform a static navigation.
   * <p>
   * Some commands calculate their navigation target when they get executed. In
   * this case only the result of the {@link #doIt()} call provides the correct
   * navigation link.
   *
   * @return The navigation link or <code>null</code>.
   */
  NaviLink getNaviLink();

  /**
   * @return <code>true</code> when the command should fail when there are invalid
   *         values within the current session.
   * @deprecated Use {@link PmCommandImpl#getBeforeDoActions()}.
   */
  @Deprecated
  boolean isRequiresValidValues();

  /**
   * Adds some decorator logic that gets applied on command execution.
   * <p>
   * May be already be called at construction time.
   *
   * @param commandDecorator The decorator to add to the command execution logic.
   */
  void addCommandDecorator(PmCommandDecorator commandDecorator);

  /**
   * @return The command (execution) state.
   */
  CommandState getCommandState();

}
