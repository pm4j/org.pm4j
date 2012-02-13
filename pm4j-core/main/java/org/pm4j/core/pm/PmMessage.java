package org.pm4j.core.pm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.api.PmLocalizeApi;

/**
 * A string resource based presentation model message.
 */
public class PmMessage {

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

  private final Severity severity;
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
  public PmMessage(PmObject pm, Severity severity, String msgKey, Object... msgArgs) {
    assert pm != null;
    assert severity != null;
    assert StringUtils.isNotBlank(msgKey);

    this.severity = severity;

    int argCount = msgArgs != null ? msgArgs.length : 0;

    // A copy of the provide arguments with the related PM as an additional default argument.
    Object [] newMsgArgs = new Object[argCount+1];
    for (int i=0; i<msgArgs.length; ++i) {
      Object arg = msgArgs[i];

      // Inject parent reference for submessages.
      if (arg instanceof SubMessageList) {
        ((SubMessageList)arg).parentMessage = this;
      }
      // a) Prevent html hacks in strings that may be entered by the user.
      // b) allow display of special characters such as '<'.
      // FIXME olaf: does not work for rich clients.
      else if (arg instanceof String) {
        arg = StringEscapeUtils.escapeHtml((String) arg);
      }

      newMsgArgs[i] = arg;
    }
    newMsgArgs[argCount] = pm.getPmTitle();
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
   * @return The related PM instance. Is never <code>null</code>.
   */
  public PmObject getPm() {
    return resourceData.pm;
  }

  public Severity getSeverity() {
    return severity;
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
     * Conctructor for message lists with item termination string.
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
}
