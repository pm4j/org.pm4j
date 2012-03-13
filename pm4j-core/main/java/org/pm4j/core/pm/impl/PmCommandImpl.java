package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmUserMessageException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviRuleLink;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.standards.PmConfirmedCommand;

/**
 * Implementation for {@link PmCommand}.
 *
 * @author olaf boede
 */
// TODO: missing doIt() signature with eventSource.
public class PmCommandImpl extends PmObjectBase implements PmCommand, Cloneable {

  private static final Log LOG = LogFactory.getLog(PmCommandImpl.class);

  /** Internal marker object. */
  protected static final String PERFORMED_REDIRECT = "- redirect -";

  /**
   * A navigation parameter that contains the PM for a dialog to start
   * after command execution.
   */
  public static final String NAVI_PARAM_NEXT_DLG_PM = "next_dialog_pm";

  /**
   * Cached list of parent commands.
   */
  private List<PmCommand> parentCommands;

  /**
   * An optional target for UI navigation after command execution.
   */
  private NaviLink naviLink;

  /** An optional command that may undo the result of this command. */
  private PmCommand undoCommand;

  /** The command logic commandDecorators to execute. */
  private Collection<PmCommandDecorator> commandDecorators = Collections.emptyList();

  /**
   * The command decorator that returned <code>false</code> for its call of
   * {@link PmCommandDecorator#beforeDo(PmCommand)}.
   * <p>
   * In other words: The decorator that prevented the execution of the
   * {@link #doItImpl()} logic of this command.
   */
  private PmCommandDecorator vetoCommandDecorator;

  /** The command (execution) state. */
  private CommandState commandState = CommandState.TEMPLATE;

  /**
   * Constructor for fix commands that have an associated field in the parent
   * PM.
   *
   * @param pmParent
   *          The presentation model that command acts on.
   */
  public PmCommandImpl(PmObject pmParent, NaviLink naviLink) {
    this(pmParent);
    this.naviLink = naviLink;
  }

  /**
   * Constructor for fix commands that have an associated field in the parent
   * PM.
   *
   * @param pmParent
   *          The presentation model that command acts on.
   */
  public PmCommandImpl(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * Defines the command that may undo this one.
   *
   * @param undoCommand
   */
  public void setUndoCommand(PmCommand undoCommand) {
    this.undoCommand = undoCommand;
  }

  /**
   * @param commandDecorator The decorator to add to the command execution logic.
   */
  @Override
  public void addCommandDecorator(PmCommandDecorator commandDecorator) {
    if (commandDecorators.isEmpty()) {
      commandDecorators = new ArrayList<PmCommandDecorator>();
    }
    commandDecorators.add(commandDecorator);
  }

  /**
   * The default implementation returns just the suggested framework navigation.
   * <p>
   * Subclasses may override this to provide alternate navigation rules.
   *
   * @param suggestedNaviLink The default navigation suggested by the framework.
   * @return The intended navigation.
   */
  protected NaviLink actionReturnOnFailure(NaviLink suggestedNaviLink) {
    return suggestedNaviLink;
  }

  @Override
  protected boolean isPmVisibleImpl() {
    if ((! isPmEnabled()) &&
        getOwnMetaData().hideWhenNotEnabled) {
      return false;
    }
    else {
      return super.isPmVisibleImpl();
    }
  }

  /**
   * The default implementation checks the own enabled flag and the enablement of
   * its parent context element.
   * <p>
   * If the command is child of a command (group), the enablement of the first
   * non-command parent will be checked.
   */
  @Override
  protected boolean isPmEnabledImpl() {
    return super.isPmEnabledImpl() &&
           getNonCmdGroupCtxt().isPmEnabled();
  }

  /**
   * A helper that is useful to get the semantic context of the command.
   *
   * @return The first non-command parent context of the command.
   */
  private PmObject getNonCmdGroupCtxt() {
    PmObject ctxt = getPmParent();
    while (ctxt instanceof PmCommand) {
      ctxt = ctxt.getPmParent();
    }
    return ctxt;
  }

  /**
   * Commands usually don't have popups. This default implementation always
   * provides an empty list for this command set kind.
   */
  @Override @SuppressWarnings("unchecked")
  public List<PmCommand> getVisiblePmCommands(CommandSet commandSet) {
    return (commandSet == CommandSet.POPUP)
              ? Collections.EMPTY_LIST
              : getVisiblePmCommands();
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  public PmCommand doIt() {
    return doIt(true);
  }

  @Override
  public PmObject doItReturnNextDlgPm() {
    PmCommand executedCmd = doIt();
    PmObject nextDlgPm = null;
    NaviLinkImpl l = (NaviLinkImpl)executedCmd.getNaviLink();
    if (l != null) {
      nextDlgPm = (PmObject) l.getNaviScopeParams().get(PmConfirmedCommand.NAVI_PARAM_NEXT_DLG_PM);
    }
    return nextDlgPm;
  }

  /**
   * See {@link #doIt().
   *
   * @param changeCommandHistory
   * @return
   */
  public PmCommand doIt(boolean changeCommandHistory) {
    PmCommandImpl cmd = zz_doCloneAndRegisterEventSource();

    if (cmd.beforeDo()) {
      NaviLink link = null;
      try {
        cmd.doItImpl();
        link = cmd.afterDo(changeCommandHistory);
        cmd.commandState = CommandState.EXECUTED;
      }
      catch (Exception e) {
        cmd.commandState = CommandState.FAILED;
        link = getPmConversationImpl().getPmExceptionHandler().onException(cmd, e, false);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Command '" + PmUtil.getPmLogString(cmd) + "' failed with exception: '" + e.getMessage() + "'.");
        }
      }
      execNavigateTo(link);
    }
    else {
      cmd.commandState = CommandState.BEFORE_DO_RETURNED_FALSE;
    }

    return cmd;
  }

  public final String doItReturnString() {
    PmCommandImpl cmd = zz_doCloneAndRegisterEventSource();
    NaviLink link = null;

    if (!cmd.beforeDo()) {
      cmd.commandState = CommandState.BEFORE_DO_RETURNED_FALSE;
      link = cmd.actionReturnOnFailure(null);
    }
    else {
      try {
        cmd.doItImpl();
        link = cmd.afterDo(true);
        cmd.commandState = CommandState.EXECUTED;
      }
      catch (Exception e) {
        cmd.commandState = CommandState.FAILED;
        link = cmd.actionReturnOnFailure(getPmConversationImpl().getPmExceptionHandler().onException(cmd, e, true));

        if (LOG.isDebugEnabled()) {
          LOG.debug("Command '" + PmUtil.getPmLogString(cmd) + "' failed with exception: '" + e.getMessage() + "'.");
        }
      }
    }

    return execNavigateTo(link);
  }

  public final void doItReturnVoid() {
    doIt();
  }

  @Override
  public final PmCommand getUndoCommand() {
    return undoCommand;
  }

  /**
   * Subclasses may implement here their concrete logic.
   *
   * @throws PmUserMessageException
   *           In case of handled failures that should be reported with a
   *           localized error message in the UI.
   * @throws Exception
   *           In case of an unexpected failure.
   */
  protected void doItImpl() throws Exception {
  }

  /**
   * Defines a target for navigation after command execution.
   * <p>
   * Does the same as {@link #setNaviLink(NaviLink)}. Provides just a more intuitive
   * 'wording' that may be used in {@link #doItImpl()}. It may express, that the
   * navigation will be performed very soon.
   *
   * @param naviLink The target to navigate to after command execution.
   */
  protected final void navigateTo(NaviLink naviLink) {
    this.naviLink = naviLink;
  }

  /**
   * Navigates to the page, the user came from.
   * <p>
   * ATTENTION: Works only in case of an enabled navigation history!
   *
   * @param linksToSkip
   *          An optional set of pages that should be skipped.<br>
   *          Example: A cancel button of a wizzard page seqence should skip the
   *          pages of the wizzard itself.
   */
  protected void navigateBack(NaviLink... linksToSkip) {
    this.naviLink = getNavigateBackLink(linksToSkip);
  }

  /**
   * Provides the link to navigate to.<br>
   * The default implementation calls {@link NaviHistory#getPrevOrStartLink()}
   * <p>
   * Subclasses may override this method to provide alternative implementations.
   *
   * @return The back-link to navigate to.<br>
   *         <code>null</code> if no navigation history is available.
   */
  protected NaviLink getNavigateBackLink(NaviLink... linksToSkip) {
    NaviHistory h = getPmConversation().getPmNaviHistory();
    if (h == null) {
      LOG.warn("No history available. Please check if the navigation history feature is enabled.");
      return null;
    }

    NaviLink prevLink = h.getPrevOrStartLink(linksToSkip);
    // FIXME: in case of some links to skip, the backPos has to be read from the
    //        history item before the link to go back to.
    String backPos = getPmConversationImpl().getViewConnector().readRequestValue(NaviLink.BACK_POS_PARAM);

    return backPos != null
          ? new NaviLinkImpl((NaviLinkImpl)prevLink, backPos)
          : prevLink;
  }

  /**
   * Defines a target for navigation after command execution.
   * <p>
   * Does the same as {@link #navigateTo(NaviLink)}. Is only more intuitive
   * 'wording' that may be used in {@link #onPmInit()}. It makes clear, that the
   * navigation will not be executed on calling this method.
   *
   * @param naviLink
   *          The target to navigate to after command execution.
   */
  protected void setNaviLink(NaviLink naviLink) {
    this.naviLink = naviLink;
  }

  /**
   * Is called on each successful command execution.
   * <p>
   * The default implementation provides a success message when
   * a string resource with the postfix "_successInfo"
   * is defined.
   * <p>
   * The message will only be added if the message was not already added
   * (e.g. by the doItImpl() method).
   */
  protected void makeOptionalSuccessMsg() {
    // First try to find a success message for the resource key base.
    // This key is usually very specific for the individual command. E.g. 'myElement.myCmd'.
    String key = getPmResKeyBase() + PmConstants.SUCCESS_MSG_KEY_POSTFIX;
    String msgTemplate = PmLocalizeApi.findLocalization(this, key);
    if (msgTemplate == null) {
      // If the specific resource base did not provide a message,
      // an assigned resource (E.g. 'common.cmdSave') key might provide a success message:
      key = getPmResKey() + PmConstants.SUCCESS_MSG_KEY_POSTFIX;
      msgTemplate = PmLocalizeApi.findLocalization(this, key);
    }

    // no success message string found.
    if (msgTemplate == null) {
      return;
    }

    // prevent message duplication:
    for (PmMessage m : PmMessageUtil.getPmInfos(this)) {
      if (m.getMsgKey().equals(key)) {
        return;
      }
    }

    // Does only pass the PM title when it is really used in the message.
    // This prevents a lot of unnecessary warnings.
    if (msgTemplate.indexOf("{0}") != -1) {
      PmMessageUtil.makeMsg(this, Severity.INFO, key, getPmParent().getPmTitle());
    }
    else {
      PmMessageUtil.makeMsg(this, Severity.INFO, key);
    }
  }


  @Override
  public CmdKind getCmdKind() {
    return getOwnMetaData().cmdKind;
  }

  @Override
  public List<PmCommand> getParentCommands() {
    if (parentCommands == null) {
      parentCommands = new ArrayList<PmCommand>();
      PmObject p = getPmParent();

      while (p instanceof PmCommand) {
        PmCommand pmCommand = (PmCommand)p;
        parentCommands.add(pmCommand);
        p = pmCommand.getPmParent();
      }

      int size = parentCommands.size();
      if (size == 0) {
        parentCommands = Collections.emptyList();
      }
      else {
        // external sort order: root first, direct parent as last.
        int halfSize = size/2;
        for (int i=0; i<halfSize; ++i) {
          Collections.swap(parentCommands, i, size-i-1);
        }
      }
    }
    return parentCommands;
  }

  @Override
  public final NaviLink getNaviLink() {
    // Ensure that 'pmInit' was called. It may define the navigation link.
    zz_ensurePmInitialization();
    return getNaviLinkImpl();
  }

  protected NaviLink getNaviLinkImpl() {
    return naviLink;
  }

  @Override
  public boolean isRequiresValidValues() {
    return getOwnMetaData().beforeDo == BEFORE_DO.VALIDATE;
  }

  // -- internal helper --


  /**
   * @return <code>true</code> when at least a single child command is enabled.
   */
  protected boolean isASubCommandEnabled() {
    List<PmCommand> subCmdList = getVisiblePmCommands();
    int subCmdNum = subCmdList.size();
    for (int i=0; i<subCmdNum; ++i) {
      if (subCmdList.get(i).isPmEnabled()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> when at least a single child command is visible.
   */
  protected boolean isASubCommandVisible() {
    List<PmCommand> subCmdList = getVisiblePmCommands();
    int subCmdNum = subCmdList.size();
    for (int i=0; i<subCmdNum; ++i) {
      if (subCmdList.get(i).isPmVisible()) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected PmCommandImpl clone() {
    try {
      return (PmCommandImpl)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new PmRuntimeException(this, "This command is not cloneable.");
    }
  }

  private PmCommandImpl zz_doCloneAndRegisterEventSource() {
    // Only the original command instance should do this once.
    // All other instances should share its meta data.
    zz_ensurePmInitialization();

    PmCommandImpl clone = clone();
    clone.commandState = CommandState.CLONED;
    return clone;
  }

  /**
   * Gets called before command execution.<br>
   * Commands may add here specific precondition validations.
   * <p>
   * The default implementation checks {@link #isRequiresValidValues()} and
   * triggers the validation of the parent element.
   */
  protected void validate() {
    PmDataInput parentElement = PmUtil.getPmParentOfType(this, PmDataInput.class);
    parentElement.pmValidate();
  }

  /**
   * Is executed before {@link #doItImpl()} gets called.
   * <p>
   * {@link #validate()} gets called in case of commands the require valid values.<br>
   * This method returns <code>false</code> if the validation fails.
   * Consequently the {@link #doItImpl()} method will not be called.
   * <p>
   * If the command does not required valid values, the current PM messages (error messages)
   * get cleared and the command gets executed.<br>
   * This matches the usual cancel-button logic.
   *
   * @return <code>true</code> if the {@link #doItImpl()} logic should be executed.
   */
  protected boolean beforeDo() {
    if (!isPmEnabled()) {
      LOG.warn("The command '" + PmUtil.getPmLogString(this)+ "' is not enabled.");
      return false;
    }

    switch (getOwnMetaData().beforeDo) {
      case VALIDATE:
        // identify existing validation errors. If one exists, the command should not be executed.
        List<PmMessage> oldNotAttributeRelatedMessages = new ArrayList<PmMessage>();
        PmConversationImpl converation = getPmConversationImpl();
        for (PmMessage m : converation.getPmMessages()) {
          if (!(m.getPm() instanceof PmAttr)) {
            oldNotAttributeRelatedMessages.add(m);
          }
        }

        validate();

        // all old non-attribute related messages can be cleared.
        // the old attribute related messages not. They contain the string conversion problems to report.
        for (PmMessage m : oldNotAttributeRelatedMessages) {
          converation.clearPmMessage(m);
        }

        List<PmMessage> errors = PmMessageUtil.getPmErrors(getPmConversation());
        if (! errors.isEmpty()) {
          if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(160);
            sb.append("Command '").append(PmUtil.getPmLogString(this)).append("' not executed because of validation errors:");
            for (PmMessage m : errors) {
              sb.append("\n\t").append(m.getTitle());
            }
            LOG.debug(sb.toString());
          }
          return false;
        }
        break;
      case CLEAR:
        PmMessageUtil.clearPmMessages(getPmConversation());
        break;
      case DO_NOTHING:
        break;
      default:
        throw new PmRuntimeException(this, "Can't handle 'beforeDo' definition: " + getOwnMetaData().beforeDo);
    }

    // XXX olaf: move to the calling methods?
    for (PmCommandDecorator d : commandDecorators) {
      if (! d.beforeDo(this)) {
        vetoCommandDecorator = d;
        return false;
      }
    }

    return true;
  }

  protected NaviLink afterDo(boolean changeCommandHistory) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Command '" + PmUtil.getPmLogString(this) + "' successfully executed.");
    }

    for (PmCommandDecorator d : commandDecorators) {
      d.afterDo(this);
    }

    // TODO olaf: that's not really always true. Subclasses should be able to control that.
    if (isRequiresValidValues()) {
      PmElement parentElement = PmUtil.getPmParentOfType(this, PmElement.class);
      new PmVisitorSetToUnchanged().visit(parentElement);
    }

    // Clear specified caches the pm-tree up till the enclosing element.
    MetaData md = getOwnMetaData();
    if (md.clearCachesSet.size() > 0) {
      PmObject pmToClear = this;
      do {
        PmCacheApi.clearCachedPmValues(pmToClear, md.clearCachesSet);
        pmToClear = pmToClear.getPmParent();
      } while (! (pmToClear instanceof PmElement));
      // Don't forget the enclosing element:
      PmCacheApi.clearCachedPmValues(pmToClear, md.clearCachesSet);
    }

    PmConversationImpl pmConversation = getPmConversationImpl();

    if (changeCommandHistory) {
      pmConversation.getPmCommandHistory().commandDone(this);
    }

    makeOptionalSuccessMsg();

    PmEventApi.firePmEvent(this, PmEvent.EXEC_COMMAND);

    return naviLink;
  }

  /**
   * Provides the command decorator that returned <code>false</code> for its call of
   * {@link PmCommandDecorator#beforeDo(PmCommand)}.
   * <p>
   * In other words: The decorator that prevented the execution of the
   * {@link #doItImpl()} logic of this command.
   *
   * @return activeCommandDecorator
   */
  public PmCommandDecorator getVetoCommandDecorator() {
    return vetoCommandDecorator;
  }

  /** {@inheritDoc} */
  @Override
  public CommandState getCommandState() {
    return commandState;
  }

  private String execNavigateTo(NaviLink link) {
    String naviString = null;

    if (link != null) {
      if (link instanceof NaviRuleLink) {
        naviString = link.getPath();
        if (LOG.isDebugEnabled()) {
          LOG.debug("Command '" + PmUtil.getPmLogString(this) + "' navigates with rule string '" + naviString + "'.");
        }
      }
      else {
        if (! (link instanceof NaviLinkImpl)) {
          throw new PmRuntimeException(this,
              "The command implementation currently only supports the following navigation link classes: " +
              NaviRuleLink.class.getName() + " and " + NaviLinkImpl.class.getName());
        }

        if (LOG.isDebugEnabled()) {
          LOG.debug("Command '" + PmUtil.getPmLogString(this) + "' redirects to '" + link + "'.");
        }

        getPmConversationImpl().getViewConnector().redirect((NaviLinkImpl)link);
      }
    }
    else {
      // TODO olaf: Check if that's still required this way.
      //            In case of a JSF history based navigation management the
      //            problem should be solved.
      //            What about the other cases?

      // Re-render current page using the original request parameter set
      // to preserve the request parameter set.
      // A simple null-string navigation would loose all parameters.
//      PmViewTechnologyConnector nh = getPmConversationImpl().getPmNavigationHandler();
//      if (nh.hasRequestParams()) {
//        nh.redirectWithRequestParams(null);
//      }
    }

    return naviString;
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);

    MetaData myMetaData = (MetaData) metaData;

    PmCommandCfg annotation = AnnotationUtil.findAnnotation(this, PmCommandCfg.class, getPmParent().getClass());
    if (annotation != null) {
      myMetaData.beforeDo = annotation.beforeDo();

      // TODO olaf: remove asap.
      if (myMetaData.beforeDo == BEFORE_DO.DEFAULT &&
          annotation.requiresValidValues() == false) {
        myMetaData.beforeDo = BEFORE_DO.CLEAR;
      }

      myMetaData.cmdKind = annotation.cmdKind();
      myMetaData.hideWhenNotEnabled = annotation.hideWhenNotEnabled();
      if (annotation.clearCaches().length > 0) {
        myMetaData.clearCachesSet = new TreeSet<PmCacheApi.CacheKind>(Arrays.asList(annotation.clearCaches()));
      }
    }

    if (myMetaData.beforeDo == BEFORE_DO.DEFAULT) {
      myMetaData.beforeDo = PmDefaults.getInstance().getBeforeDoCommandDefault();
    }
  }

  /**
   * Shared meta data for all commands of the same kind.
   * E.g. for all 'myapp.User.cmdSave' attributes.
   */
  protected static class MetaData extends PmObjectBase.MetaData {
    private PmCommandCfg.BEFORE_DO beforeDo = BEFORE_DO.DEFAULT;
    private CmdKind cmdKind = CmdKind.COMMAND;
    private Set<PmCacheApi.CacheKind> clearCachesSet = Collections.emptySet();
    /**
     * Should the command be hidden when not applicable. Defaults to <code>false</code>.
     */
    private boolean hideWhenNotEnabled = false;

    public boolean isHideWhenNotEnabled() {
      return hideWhenNotEnabled;
    }
    public void setHideWhenNotEnabled(boolean hideWhenNotEnabled) {
      this.hideWhenNotEnabled = hideWhenNotEnabled;
    }
    public CmdKind getCmdKind() {
      return cmdKind;
    }
    public void setCmdKind(CmdKind cmdKind) {
      this.cmdKind = cmdKind;
    }
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}
