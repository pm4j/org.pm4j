package org.pm4j.core.pm.api;

import java.util.List;
import java.util.TreeSet;

import org.pm4j.common.util.collection.ArrayUtil;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmUserMessageException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;

public class PmMessageUtil {

  /**
   * Generates a message and propagates it to the PM conversation.
   *
   * @param severity
   *          Message severity.
   * @param key
   *          The message resource key.
   * @param resArgs
   *          The arguments for the resource string.
   * @return The generated message.
   *
   * @deprecated use {@link PmMessageApi#addMessage(PmObject, Severity, String, Object...)}
   */
  public static PmMessage makeMsg(PmObject pm, Severity severity, String key, Object... resArgs) {
    return PmMessageApi.addMessage(pm, severity, key, resArgs);
  }

  /**
   * Generates a standard exception message and propagates it to the PM conversation.
   *
   * @param pm The exception related PM.
   * @param severity
   *          Message severity.
   * @param key
   *          The message resource key.
   * @param resArgs
   *          The arguments for the resource string.
   * @return The generated message.
   */
  public static PmMessage makeExceptionMsg(PmObject pm, Severity severity, Throwable e) {
    PmMessage msg = null;
    if (e instanceof PmUserMessageException) {
      // XXX olaf: internalize handling of not internationalized strings: use a standard resource that just provides the message.
      PmResourceData rd = ((PmUserMessageException) e).getResourceData();
      msg = (rd != null)
          ? new PmMessage(pm, severity, rd.msgKey, rd.msgArgs)
          : new PmMessage(pm, severity, e, PmConstants.MSGKEY_FIRST_MSG_PARAM, e.getMessage());
    }
    else {
      msg = new PmMessage(pm, severity, e, PmConstants.MSGKEY_FIRST_MSG_PARAM, e.getMessage());
    }
    pm.getPmConversation().addPmMessage(msg);
    return msg;
  }

  /**
   * Generates an INFO message and propagates it to the PM session
   * @param keybase The message resource key
   * @param number depending on the number the postfix 'one' or 'many' is added to keybase
   * @param resArgs The arguments for the resource string.
   * @return The generated message.
   */
  public static PmMessage makeMsgOneMany(PmObject pm, String keybase, int number, Object... resArgs) {
    return makeMsgOneMany(pm, Severity.INFO, keybase, number, resArgs);
  }

  /**
   * Generates a message and propagates it to the PM session
   * @param severity Message severity.
   * @param keybase The message resource key
   * @param number depending on the number the postfix 'one' or 'many' is added to keybase
   * @param resArgs The arguments for the resource string.
   * @return The generated message.
   */
  public static PmMessage makeMsgOneMany(PmObject pm, Severity severity, String keybase, int number, Object... resArgs) {
    // TODO: change to '_one' and '_many' to prevent naming conflicts.
    // TODO: add _none
    String msgKey = keybase + (number > 1 ? "many" : "one");

    Object[] resArgsWithNumber = ArrayUtil.copyOf(resArgs, resArgs.length+1, 1);
    resArgsWithNumber[0] = number;

    PmMessage msg = new PmMessage(pm, severity, msgKey, resArgsWithNumber);
    pm.getPmConversation().addPmMessage(msg);
    return msg;
  }

  /**
   * Provides a success message when a string resource for the given key is
   * defined.
   *
   * @param key
   *          The resource key to be searched for.
   * @param msgArgs
   *          Optional message arguments.
   */
  public static void makeOptionalInfoMsg(PmObject pm, String key, Object... msgArgs) {
    String msgString = PmLocalizeApi.findLocalization(pm, key);
    if (msgString != null) {
      PmMessageApi.addMessage(pm, Severity.INFO, key, msgArgs);
    }
  }

  /**
   * Generates a warning that indicates that a required value is not provided.
   * <p>
   * For attributes with options the resource
   * {@link #MSGKEY_VALIDATION_MISSING_REQUIRED_SELECTION} is used.<br>
   * For attributes without options the resource
   * {@link #MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE} is used.
   * <p>
   * When a custom resource key with the postfix
   * {@link #RESKEY_POSTFIX_REQUIRED_MSG} is provided, that key will be used for
   * message generation.
   *
   * @return The resource data for the required attribute value warning.
   *
   * @deprecated use {@link PmMessageApi#makeRequiredMessageResData(PmAttr)}
   */
  public static PmResourceData makeRequiredWarning(PmAttr<?> pm) {
    return PmMessageApi.makeRequiredMessageResData(pm);
  }

  /**
   * @param pm
   * @return
   *
   * @deprecated use {@link PmMessageApi#getMessages(PmObject)}
   */
  public static List<PmMessage> getPmMessages(PmObject pm) {
    return PmMessageApi.getMessages(pm);
  }

  /**
   * @return Error messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   *
   * @deprecated Please use {@link PmMessageApi#getMessages(PmObject, Severity)} with {@link Severity#ERROR}.
   */
  public static List<PmMessage> getPmErrors(PmObject pm) {
    return PmMessageApi.getMessages(pm, Severity.ERROR);
  }

  /**
   * @return Warning messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   *
   * @deprecated Please use {@link PmMessageApi#getMessages(PmObject, Severity)} with {@link Severity#WARN}.
   */
  public static List<PmMessage> getPmWarnings(PmObject pm) {
    return PmMessageApi.getMessages(pm, Severity.WARN);
  }

  /**
   * @return Info messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   *
   * @deprecated Please use {@link PmMessageApi#getMessages(PmObject, Severity)} with {@link Severity#INFO}.
   */
  public static List<PmMessage> getPmInfos(PmObject pm) {
    return PmMessageApi.getMessages(pm, Severity.INFO);
  }

  /**
   * Finds the most severity message starting from root pm with at least
   * minSeverity. If there are several messages of most severe level, the one
   * whose message text is alphabetically first will be returned.
   *
   * @param pm
   *          The Root PM sub tree to get the most severe message for.
   * @param minSeverity
   *          The minimal message severity to consider.
   *
   * @return The most severe message for the given PM or <code>null</code> if
   *         there is no message for the given PM.
   */
  public static PmMessage findMostSevereMessage(PmObject pm, Severity minSeverity) {
    TreeSet<PmMessage> messages = new TreeSet<PmMessage>(new PmMessage.MessageComparator());
    messages.addAll(PmMessageApi.getPmTreeMessages(pm, minSeverity));

    return messages.isEmpty()
        ? null
        : messages.iterator().next();
  }

  /**
   * Finds the most severity message starting from root pm. If there are several
   * messages of most severe level, the one whose message text is alphabetically
   * first will be returned.
   *
   * @param pm
   *          The Root PM sub tree to get the most severe message for.
   *
   * @return The most severe message for the given PM or <code>null</code> if
   *         there is no message for the given PM.
   */
  public static PmMessage findMostSevereMessage(PmObject pm) {
    return findMostSevereMessage(pm, Severity.INFO);
  }

  /**
   * Clears the messages within the scope and returns all messages for this PM
   * scope existing before this call.
   * <p>
   * Any invalid attribute values within the PM scope are also cleaned by
   * cleaning the corresponding error messages.
   *
   * @return All messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   *
   * @deprecated use {@link PmMessageApi#clearPmTreeMessages(PmObject)
   */
  public static List<PmMessage> clearSubTreeMessages(PmObject pm) {
    return PmMessageApi.clearPmTreeMessages(pm);
  }

  /**
   * Provides the messages of a PM sub tree.
   *
   * @param pm Root of the PM sub tree to check.
   * @param minSeverity The minimal message severity to consider.
   * @return
   *
   * @deprecated use {@link PmMessageApi#getPmTreeMessages(PmObject, Severity)
   */
  public static List<PmMessage> getSubTreeMessages(PmObject pm, Severity minSeverity) {
    return PmMessageApi.getPmTreeMessages(pm, minSeverity);
  }

}