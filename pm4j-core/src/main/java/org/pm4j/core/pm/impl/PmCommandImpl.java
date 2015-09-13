package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmUserMessageException;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.AFTER_DO;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviRuleLink;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for {@link PmCommand}.
 *
 * @author olaf boede
 */
public class PmCommandImpl extends PmObjectBase implements PmCommand, Cloneable {

  private static final Logger LOG = LoggerFactory.getLogger(PmCommandImpl.class);

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
  /* package */ PmCommandDecoratorSetImpl commandDecorators = new PmCommandDecoratorSetImpl();

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
   * A processed command (after starting {@link #doItImpl()}) was cloned from a
   * template. In this state this member identifies the clone source.
   */
  private PmCommandImpl templateCommand;

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
   * @param commandDecorator The decorator to add to the command execution logic.
   */
  @Override
  public void addCommandDecorator(PmCommandDecorator commandDecorator) {
    commandDecorators.addDecorator(commandDecorator);
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

  public final PmCommand doIt() {
    return doIt(true);
  }

  /**
   * See {@link #doIt().
   *
   * @param changeCommandHistory
   * @return
   */
  public PmCommand doIt(boolean changeCommandHistory) {
    PmCommandImpl cmd = clone();

    if (cmd._beforeDo()) {
      NaviLink link = null;
      try {
        cmd.doItImpl();
        link = cmd.afterDo(changeCommandHistory);
        cmd.commandState = CommandState.EXECUTED;
      }
      catch (Exception e) {
        cmd.commandState = CommandState.FAILED;

        // The standard exception handling can be prevented by a decorator that
        // says that it already handeled everything.
        boolean useStandardExceptionHandling = true;
        Exception exceptionToHandle = e;

        try {
          useStandardExceptionHandling = commandDecorators.onException(this, e);
        } catch (RuntimeException e2) {
          LOG.info(PmUtil.getPmLogString(cmd) + ": Exception thrown by a command decorator while handling a catched exception '" + this + "'", e);
          exceptionToHandle = e2;
        }

        if (useStandardExceptionHandling) {
          link = getPmConversationImpl().getPmExceptionHandler().onException(cmd, exceptionToHandle, false);
        }

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

  // TODO olaf: 80% code copy of doIt(boolean)...
  public final String doItReturnString() {
    PmCommandImpl cmd = clone();
    NaviLink link = null;

    if (!cmd._beforeDo()) {
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

        // The standard exception handling can be prevented by a decorator that
        // says that it already handeled everything.
        boolean useStandardExceptionHandling = true;
        Exception exceptionToHandle = e;

        try {
          useStandardExceptionHandling = commandDecorators.onException(this, e);
        } catch (RuntimeException e2) {
          LOG.info(PmUtil.getPmLogString(cmd) + ": Exception thrown by a command decorator while handling a catched exception '" + this + "'", e);
          exceptionToHandle = e2;
        }

        if (useStandardExceptionHandling) {
          link = getPmConversationImpl().getPmExceptionHandler().onException(cmd, exceptionToHandle, true);
        }

        link = cmd.actionReturnOnFailure(link);


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
  public PmCommand getUndoCommand() {
    return undoCommand;
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
   * Subclasses may implement here their concrete logic.
   *
   * @throws PmUserMessageException
   *           In case of handled failures that should be reported with a
   *           localized error message in the UI.
   */
  protected void doItImpl() {
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
    String backPos = getPmConversationImpl().getPmToViewTechnologyConnector().readRequestValue(NaviLink.BACK_POS_PARAM);

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
    for (PmMessage m : PmMessageApi.getMessages(this, Severity.INFO)) {
      if (m.getMsgKey().equals(key)) {
        return;
      }
    }

    // Does only pass the PM title when it is really used in the message.
    // This prevents a lot of unnecessary warnings.
    if (msgTemplate.indexOf("{0}") != -1) {
      PmMessageApi.addMessage(this, Severity.INFO, key, getPmParent().getPmTitle());
    }
    else {
      PmMessageApi.addMessage(this, Severity.INFO, key);
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
  @Deprecated
  public boolean isRequiresValidValues() {
    return getBeforeDoActions().contains(BEFORE_DO.VALIDATE);
  }

  // -- internal helper --


  /**
   * @return <code>true</code> when at least a single child command is enabled.
   */
  protected boolean isASubCommandEnabled() {
    List<PmCommand> subCmdList = PmUtil.getVisiblePmCommands(this);
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
    List<PmCommand> subCmdList = PmUtil.getVisiblePmCommands(this);
    int subCmdNum = subCmdList.size();
    for (int i=0; i<subCmdNum; ++i) {
      if (subCmdList.get(i).isPmVisible()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public PmCommandImpl clone() {
    PmCommandImpl clone = (PmCommandImpl)super.clone();
    clone.templateCommand = this;
    clone.commandState = CommandState.CLONED;
    clone.commandDecorators = commandDecorators.clone();
    return clone;
  }

  /**
   * Gets called before command execution.<br>
   * Commands may add here specific precondition validations.
   * <p>
   * The default implementation checks {@link #isRequiresValidValues()} and
   * triggers the validation of the parent element.
   *
   * @return <code>true</code> if the validation was successful. That means: There are no errors related to
   * {@link #getValidationErrorRootPm()}.
   */
  protected boolean validate() {
    return PmValidationApi.validateSubTree(getValidationExecRootPm(), getValidationErrorRootPm());
  }

  /**
   * @return Defines what happens before {@link #doItImpl()} gets called.
   */
  protected Set<BEFORE_DO> getBeforeDoActions() {
    return getOwnMetaData().beforeDo;
  }

  /**
   * @return Defines what happens after {@link #doItImpl()} gets called.
   */
  protected Set<AFTER_DO> getAfterDoActions() {
    return getOwnMetaData().afterDo;
  }

  /**
   * Provides the object that defines the area (PM sub tree) that should not contain
   * an error before the command gets executed.
   * <p>
   * It will only be considered if the command validates before execution.
   * <p>
   * The default implementation provides the related {@link PmConversation}.
   *
   * @return The root object of the PM subtree that should be valid for this command.
   */
  protected PmObject getValidationErrorRootPm() {
    return getPmConversation();
  }

  /**
   * Provides the object that defines the area (PM sub tree) the validation should be
   * called for before command execution.
   * <p>
   * It will only be considered if the command validates before execution.
   * <p>
   * The default implementation provides the next parent that is not of type {@link PmCommand}.
   *
   * @return the PM tree root object to validate before execution.
   */
  protected PmObject getValidationExecRootPm() {
    PmObject o = getPmParent();
    while (o instanceof PmCommand) {
      o = o.getPmParent();
    }
    return o;
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

    PmConversationImpl conversation = getPmConversationImpl();

    // Clear any message related to this command. A message for a failed previous execution attempt
    // should not prevent an attempt to try it again...
    conversation.clearPmMessages(this, null);

    Set<BEFORE_DO> beforeDoAction = getBeforeDoActions();
    if (beforeDoAction.contains(BEFORE_DO.VALIDATE)) {
      if (! validate()) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Command '" + PmUtil.getPmLogString(this) + "' was not executed because of validation errors.");
        }
        return false;
      }
    }
    if (beforeDoAction.contains(BEFORE_DO.CLEAR)) {
      PmMessageApi.clearPmTreeMessages(getValidationErrorRootPm());
    }

    return true;
  }

  protected NaviLink afterDo(boolean changeCommandHistory) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Command '" + PmUtil.getPmLogString(this) + "' successfully executed.");
    }

    if (getAfterDoActions().contains(AFTER_DO.RESET_VALUE_CHANGED_STATE)) {
      PmObject validationParentPm = getValidationExecRootPm();
      validationParentPm.setPmValueChanged(false);
    }

    // Clear specified caches the pm-tree up till the enclosing element.
    if (getAfterDoActions().contains(AFTER_DO.CLEAR_CACHES)) {
      MetaData md = getOwnMetaData();
      if (md.clearCachesSet.size() > 0) {
        PmObject pmToClear = this;
        do {
          PmCacheApi.clearPmCache(pmToClear, md.clearCachesSet);
          pmToClear = pmToClear.getPmParent();
        } while (pmToClear instanceof PmCommand);
        // Don't forget the enclosing non-command parent PM:
        PmCacheApi.clearPmCache(pmToClear, md.clearCachesSet);
      }
    }


    PmConversationImpl pmConversation = getPmConversationImpl();

    if (changeCommandHistory) {
      pmConversation.getPmCommandHistory().commandDone(this);
    }

    makeOptionalSuccessMsg();
    commandDecorators.afterDo(this);

    PmEventApi.firePmEvent(this, PmEvent.EXEC_COMMAND);

    return naviLink;
  }

  /** Calls domain specific {@link #beforeDo()} and after that the before-do logic of configured decorators. */
  boolean _beforeDo() {
    if (!beforeDo()) {
      return false;
    } else {
      vetoCommandDecorator = commandDecorators.beforeDoReturnVetoDecorator(this);
      // before-do was successful if all decorators agree.
      return vetoCommandDecorator == null;
    }
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

        getPmConversationImpl().getPmToViewTechnologyConnector().redirect((NaviLinkImpl)link);
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

  /**
   * A processed command (after starting {@link #doIt()}) gets cloned from the
   * original command instance (the template).<br>
   * In this state this member identifies the clone source.
   *
   * @return the template instance or <code>null</code> if it's the original
   *         command instance.
   */
  public PmCommand getTemplateCommand() {
    return templateCommand;
  }

  /** Clears cached template instance values too. */
  @Override
  protected void clearCachedPmValues(Set<CacheKind> cacheSet) {
    super.clearCachedPmValues(cacheSet);
    if (templateCommand != null) {
      templateCommand.clearCachedPmValues(cacheSet);
    }
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
      myMetaData.beforeDo = new HashSet<BEFORE_DO>(Arrays.asList(annotation.beforeDo()));
      myMetaData.afterDo = new HashSet<AFTER_DO>(Arrays.asList(annotation.afterDo()));
      myMetaData.cmdKind = annotation.cmdKind();
      if (annotation.clearCaches().length > 0) {
        myMetaData.clearCachesSet = new TreeSet<PmCacheApi.CacheKind>(Arrays.asList(annotation.clearCaches()));
      }
    } else {
      myMetaData.beforeDo = new HashSet<BEFORE_DO>(Arrays.asList(new BEFORE_DO[] {BEFORE_DO.DEFAULT}));
      myMetaData.afterDo = new HashSet<AFTER_DO>(Arrays.asList(new AFTER_DO[] {AFTER_DO.DEFAULT}));
    }

    if (myMetaData.beforeDo.contains(BEFORE_DO.DEFAULT)) {
      if (myMetaData.beforeDo.size() > 1) {
        throw new PmRuntimeException(this, "beforeDo can not contain other values if default is specified.");
      }
      myMetaData.beforeDo = PmDefaults.getInstance().getBeforeDoCommandDefault();
    }
    if (myMetaData.afterDo.contains(AFTER_DO.DEFAULT)) {
      if (myMetaData.afterDo.size() > 1) {
          throw new PmRuntimeException(this, "afterDo can not contain other values if default is specified.");
      }
      if (myMetaData.beforeDo.contains(BEFORE_DO.VALIDATE)) {
        myMetaData.afterDo.add(AFTER_DO.RESET_VALUE_CHANGED_STATE);
      }
      myMetaData.afterDo.add(AFTER_DO.CLEAR_CACHES);
    }
  }

  /**
   * Shared meta data for all commands of the same kind.
   * E.g. for all 'myapp.User.cmdSave' attributes.
   */
  protected static class MetaData extends PmObjectBase.MetaData {
    private Set<PmCommandCfg.BEFORE_DO> beforeDo;
    private Set<PmCommandCfg.AFTER_DO> afterDo;
    private CmdKind cmdKind = CmdKind.COMMAND;
    private Set<PmCacheApi.CacheKind> clearCachesSet = Collections.emptySet();
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

  // FIXME oboede: methods needed to identify direct usages (which shouldn't exist in the old code)
  // Make base methods final and remove this implementations after doing that.
  @Override
  public boolean isPmValueChanged() {
    return super.isPmValueChanged();
  }

  @Override
  public void setPmValueChanged(boolean changed) {
    super.setPmValueChanged(changed);
  }

  @Override
  public void resetPmValues() {
    super.resetPmValues();
  }

}
