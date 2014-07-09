package org.pm4j.core.pm.api;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * API to deal with messages.
 *
 * @author sdolke
 *
 */
public class PmMessageApi {

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
   */
  public static PmMessage addMessage(PmObject pm, Severity severity, String key, Object... resArgs) {
    PmMessage msg = new PmMessage(pm, severity, key, resArgs);
    pm.getPmConversation().addPmMessage(msg);
    return msg;
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
  public static PmResourceData makeRequiredMessageResData(PmAttr<?> pm) {
    PmAttrBase<?, ?> pmImpl = (PmAttrBase<?, ?>) pm;
    String msgKey = pmImpl.getPmResKey() + PmConstants.RESKEY_POSTFIX_REQUIRED_MSG;
    String customMsg = PmLocalizeApi.findLocalization(pmImpl, msgKey);

    if (customMsg == null) {
      msgKey = (pmImpl.getOptionSet().getOptions().size() == 0) ? PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE
          : PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_SELECTION;
    }

    return new PmResourceData(pm, msgKey, pm.getPmTitle());
  }

  /** @deprecated Please use {@link #makeRequiredMessageResData(PmAttr)}. */
  @Deprecated
  public static PmResourceData addRequiredMessage(PmAttr<?> pm) {
      return makeRequiredMessageResData(pm);
  }

  /**
   * @return List of all messages that are related to the given presentation
   *         model.<br>
   *         In case of no messages an empty collection.
   */
  public static List<PmMessage> getMessages(PmObject pm) {
    return pm.getPmConversation().getPmMessages(pm, null);
  }

  /**
   * @return List of messages with the given severity that are related to the
   *         given presentation model.<br>
   *         In case of no messages an empty collection.
   */
  public static List<PmMessage> getMessages(PmObject pm, Severity severity) {
    return pm.getPmConversation().getPmMessages(pm, severity);
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
  public static List<PmMessage> clearPmTreeMessages(PmObject pm) {
    PmConversationImpl pmConversation = (PmConversationImpl) pm.getPmConversation();

    List<PmMessage> messages = getPmTreeMessages(pm, Severity.INFO);
    for (PmMessage m : messages) {
      pmConversation.clearPmMessage(m);
    }

    return messages;
  }

  /**
   * Provides the messages of a PM sub tree.
   *
   * @param pm
   *          Root of the PM sub tree to check.
   * @param minSeverity
   *          The minimal message severity to consider.
   * @return
   */
  public static List<PmMessage> getPmTreeMessages(PmObject pm, Severity minSeverity) {
    List<PmMessage> messages = new ArrayList<PmMessage>();

    for (PmMessage m : pm.getPmConversation().getPmMessages()) {
      if (m.getSeverity().ordinal() >= minSeverity.ordinal() && (m.isMessageFor(pm) || PmUtil.isChild(pm, m.getPm()))) {
        messages.add(m);
      }
    }

    return messages;
  }

}
