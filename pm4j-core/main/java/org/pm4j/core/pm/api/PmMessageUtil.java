package org.pm4j.core.pm.api;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.util.collection.ArrayUtil;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmUtil;

public class PmMessageUtil {

  /**
   * Generates a message and propagates it to the PM session.
   *
   * @param severity
   *          Message severity.
   * @param key
   *          The message resource key.
   * @param resArgs
   *          The arguments for the resource string.
   * @return The generated message.
   */
  public static PmMessage makeMsg(PmObject pm, Severity severity, String key, Object... resArgs) {
    PmMessage msg = new PmMessage(pm, severity, key, resArgs);
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
    String msgString = ((PmObjectBase)pm).findLocalization(key);
    if (msgString != null) {
      PmMessageUtil.makeMsg(pm, Severity.INFO, key, msgArgs);
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
   */
  public static PmResourceData makeRequiredWarning(PmAttr<?> pm) {
    PmAttrBase<?, ?> pmImpl = (PmAttrBase<?, ?>)pm;
    String msgKey = pmImpl.getPmResKeyBase() + PmConstants.RESKEY_POSTFIX_REQUIRED_MSG;
    String customMsg = pmImpl.findLocalization(msgKey);

    if (customMsg == null) {
      msgKey = (pmImpl.getOptionSet().getOptions().size() == 0)
                  ? PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE
                  : PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_SELECTION;
    }

    return new PmResourceData(pm, msgKey, pm.getPmShortTitle());
  }


  public static List<PmMessage> getPmMessages(PmObject pm) {
    return pm.getPmConversation().getPmMessages(pm, null);
  }

  /**
   * @return Error messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   */
  public static List<PmMessage> getPmErrors(PmObject pm) {
    return pm.getPmConversation().getPmMessages(pm, Severity.ERROR);
  }

  /**
   * @return Warning messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   */
  public static List<PmMessage> getPmWarnings(PmObject pm) {
    return pm.getPmConversation().getPmMessages(pm, Severity.WARN);
  }

  /**
   * @return Info messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   */
  public static List<PmMessage> getPmInfos(PmObject pm) {
    return pm.getPmConversation().getPmMessages(pm, Severity.INFO);
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
   */
  public static List<PmMessage> clearPmMessages(PmObject pm) {
    PmEventApi.ensureThreadEventSource(pm);
    List<PmMessage> messages = new ArrayList<PmMessage>(getPmMessages(pm));
    ((PmConversationImpl)pm.getPmConversation()).clearPmMessages(pm, null);

    for (PmObject p : PmUtil.getPmChildren(pm)) {
      messages.addAll(PmMessageUtil.clearPmMessages(p));
    }

    return messages;
  }

  /**
   * Clears not yet validated values within the scope of this PM.
   */
  public static void clearPmInvalidValues(PmObject pm) {
    ((PmObjectBase)pm).clearPmInvalidValues();
  }


}
