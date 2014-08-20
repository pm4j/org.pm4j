package org.pm4j.core.pm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmCommandImpl;

/**
 * A string resource based presentation model message.
 */
public class PmMessage {

  private static final Log LOG = LogFactory.getLog(PmMessage.class);

  public enum Severity {
    /** Normal user feedback. */
    INFO,
    /**
     * A feedback that should be presented noticeable.
     * The existence of a warning does not prevent a command execution.
     */
    WARN,
    /**
     * A feedback that should be presented noticeable.
     * The existence of an error prevents the execution of commands that require valid values.
     */
    ERROR;
    /**
     * A feedback that should be presented noticeable.
     * The existence of an fatal error prevents the execution of commands that require valid values.
     * TODO: FATAL Severity for internal errors
    FATAL*/

    /**
     * @return The CSS style class that is associated with this severity.<br>
     *         Provides the lower case of the enum value.
     */
    public String getStyleClass() {
      switch (this) {
        case INFO:  return PmObject.STYLE_CLASS_INFO;
        case WARN:  return PmObject.STYLE_CLASS_WARN;
        case ERROR: return PmObject.STYLE_CLASS_ERROR;
        default: throw new PmRuntimeException("Unknown message severty: " + this);
      }
    }
  };

  private Severity severity;
  private final PmResourceData resourceData;
  private Throwable cause;

  /**
   * @param pm
   *          The PM this message is related to.
   * @param severity
   *          Message severity.
   * @param msgKey
   *          The resource string key.
   * @param msgArgs
   *          Arguments for the resource string.
   */
  public PmMessage(final PmObject pm, Severity severity, String msgKey, Object... msgArgs) {
    assert pm != null;
    assert severity != null;
    assert StringUtils.isNotBlank(msgKey);

    this.severity = severity;

    int argCount = msgArgs != null ? msgArgs.length : 0;

    // A copy of the provide arguments with the related PM as an additional default argument.
    Object [] newMsgArgs = new Object[argCount + 1];
    for (int i=0; i<msgArgs.length; ++i) {
      Object arg = msgArgs[i];

      // Inject parent reference for submessages.
      if (arg instanceof SubMessageList) {
        ((SubMessageList)arg).parentMessage = this;
      }

      newMsgArgs[i] = arg;
    }

    // The title gets provided by a proxy to prevent problems with PM initialization states.
    // It also prevents unnecessary getPmTitle() calls if the title is not relevant for the
    // resource string.
    newMsgArgs[argCount] = new Object() {
      @Override
      public String toString() {
        try {
          return pm.getPmTitle();
        }
        catch (RuntimeException e) {
          // a fall back if the title is not accessible:
          LOG.info("Unable to resolve a title parameter for a message. Related PM: " + pm.getPmRelativeName());
          return pm.getPmRelativeName();
        }
      }
    };

    this.resourceData = new PmResourceData(pm, msgKey, newMsgArgs);
  }

  /**
   * Constructor for messages that are caused by an exception.
   *
   * @param pm
   *          The PM this message is related to.
   * @param severity
   *          Message severity.
   * @param cause
   *          An optional exception that caused this message.
   * @param msgKey
   *          The resource string key.
   * @param msgArgs
   *          Arguments for the resource string.
   */
  public PmMessage(PmObject pm, Severity severity, Throwable cause, String msgKey, Object... msgArgs) {
    this(pm, severity, msgKey, msgArgs);
    this.cause = cause;
  }

  /**
   * Constructor {@link PmResourceData} based messages.
   *
   * @param pm
   *          The PM this message is related to.
   * @param severity
   *          Message severity.
   * @param msgResourceData Message resource data.
   */
  public PmMessage(PmObject pm, Severity severity, PmResourceData msgResourceData) {
    this(pm, severity, msgResourceData.msgKey, msgResourceData.msgArgs);
  }

  /**
   * @return The related PM instance. Is never <code>null</code>.
   */
  public PmObject getPm() {
    return resourceData.pm;
  }

  public Severity getSeverity() {
    return severity;
  }

  /**
   * Checks if the message is related to the given PM.
   * <p>
   * This is especially for commands not only a simple compare operation. It has to consider
   * that commands get cloned before execution. Any message created for the clone is then
   * also relevant for the original command.
   *
   * @param pmToCheck The PM to check.
   * @return <code>true</code> if the message is related to the given PM.
   */
  public boolean isMessageFor(PmObject pmToCheck) {
    PmObject pm = getPm();
    if (pmToCheck == pm) {
      return true;
    }

    if (pm instanceof PmCommandImpl && pmToCheck instanceof PmCommandImpl) {
      PmObject templateOfMessageCommand = ((PmCommandImpl)pm).getTemplateCommand();
      PmObject templateOfCommandToCheck = ((PmCommandImpl)pmToCheck).getTemplateCommand();
      return templateOfMessageCommand == pmToCheck ||
             templateOfMessageCommand == templateOfCommandToCheck ||
             pm == templateOfCommandToCheck;
    }

    // does not match.
    return false;
  }

  /**
   * @return <code>true</code> in case of severity {@link Severity#ERROR}.
   */
  public boolean isError() {
    return severity == Severity.ERROR;
  }

  /**
   * @return <code>true</code> in case of severity {@link Severity#WARN}.
   */
  public boolean isWarning() {
    return severity == Severity.WARN;
  }

  /**
   * @return <code>true</code> in case of severity {@link Severity#INFO}.
   */
  public boolean isInfo() {
    return severity == Severity.INFO;
  }

  public String getMsgKey() {
    return resourceData.msgKey;
  }

  public Object[] getMsgArgs() {
    return resourceData.msgArgs;
  }

  public String getTitle() {
    return localize(resourceData.msgKey, resourceData.msgArgs);
  }

  public String getTooltip() {
    return localizeOptional(resourceData.msgKey + PmConstants.RESKEY_POSTFIX_TOOLTIP, resourceData.msgArgs);
  }

  /**
   * Defines a message causing exception.
   *
   * @param cause The exception that caused this message.
   */
  public void setCause(Throwable cause) {
    this.cause = cause;
  }

  /**
   * @return An optional exception that caused this message.
   */
  public Throwable getCause() {
    return cause;
  }

  @Override
  public String toString() {
    try {
      return getTitle();
    }
    catch (RuntimeException e) {
      return "PmMessage key=" + resourceData.msgKey +
              (resourceData.msgArgs.length > 0
                   ? " args=" + resourceData.msgArgs
                   : "");
    }
  }

  protected String localize(String resKey, Object... resStringArgs) {
    Object[] args = getArgsWithSubMessages(resStringArgs);
    return PmLocalizeApi.localize(resourceData.pm, resKey, args);
  }

  protected String localizeOptional(String resKey, Object... resStringArgs) {
    Object[] args = getArgsWithSubMessages(resStringArgs);
    return PmLocalizeApi.findLocalization(resourceData.pm, resKey, args);
  }

  private Object[] getArgsWithSubMessages(Object... resStringArgs) {
    Object[] args = new Object[resStringArgs.length];
    for (int i=0; i<resStringArgs.length; ++i) {
      args[i] = (resStringArgs[i] instanceof SubMessageList)
          ? ((SubMessageList)resStringArgs[i]).getTitle()
          : resStringArgs[i];
    }
    return args;
  }

  /**
   * A list that can be used to pass a set of submessages as message argument.
   * All items of the {@link SubMessageList} are rendered at the render position
   * specified in the resource string of the enclosing message.
   */
  public static class SubMessageList {
    private PmMessage parentMessage;
    private String termStringKey;
    private List<PmMessage> messageList;

    /**
     * Conctructor for message lists without item termination string.
     *
     * @param parentMessage Parent of this list. Used to get context information (language etc.)
     */
    public SubMessageList() {
      this(null);
    }

    /**
     * Constructor for message lists with item termination string.
     *
     * @param termStringKey Key of a string termination character sequence to be placed between the items.
     * @param parentMessage Parent of this list. Used to get context information (language etc.)
     */
    public SubMessageList(String termStringKey) {
      this.termStringKey = termStringKey;
      this.messageList = new ArrayList<PmMessage>();
    }

    /**
     * Add as message subitem.
     *
     * @param msgKey Resource key of the message item.
     * @param msgArgs Resource string arguments for the message item.
     */
    public void add(String msgKey, Object... msgArgs) {
      messageList.add(new PmMessage(parentMessage.getPm(), Severity.INFO, msgKey, msgArgs));
    }

    /**
     * @return The language specific string for the complete list.
     */
    public String getTitle() {
      StringBuilder sb = new StringBuilder();
      String termString = (termStringKey != null) ? parentMessage.localize(termStringKey) : "";

      for (PmMessage m : messageList) {
        if (sb.length() > 0) {
          sb.append(termString);
        }
        sb.append(m.getTitle());
      }

      return sb.toString();
    }

  }
  
  /**
   * Set the severity.
   * @param severity the new severity
   */
  public void setSeverity(Severity severity) {
    this.severity = severity;
  }
}
