package org.pm4j.core.pm.api;

import java.util.List;

import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;

/**
 * @deprecated use {@link PmMessageApi} instead
 */
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
   * @deprecated use {@link PmMessageApi#addMsg(PmObject, Severity, String, Object...)}
   */
  public static PmMessage makeMsg(PmObject pm, Severity severity, String key, Object... resArgs) {
    return PmMessageApi.addMsg(pm, severity, key, resArgs);
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
   * 
   * @deprecated use {@link PmMessageApi#addExceptionMsg(PmObject, Severity, Throwable)}
   */
  public static PmMessage makeExceptionMsg(PmObject pm, Severity severity, Throwable e) {
    return PmMessageApi.addExceptionMsg(pm, severity, e);
  }

  /**
   * Generates an INFO message and propagates it to the PM session
   * 
   * @param keybase The message resource key
   * @param number depending on the number the postfix 'one' or 'many' is added to keybase
   * @param resArgs The arguments for the resource string.
   * @return The generated message.
   * 
   * @deprecated use {@link PmMessageApi#addMsgOneMany(PmObject, String, int, Object...)}
   */
  public static PmMessage makeMsgOneMany(PmObject pm, String keybase, int number, Object... resArgs) {
    return PmMessageApi.addMsgOneMany(pm, keybase, number, resArgs);
  }

  /**
   * Generates a message and propagates it to the PM session
   * 
   * @param severity Message severity.
   * @param keybase The message resource key
   * @param number depending on the number the postfix 'one' or 'many' is added to keybase
   * @param resArgs The arguments for the resource string.
   * @return The generated message.
   * 
   * @deprecated use {@link PmMessageApi#addMsgOneMany(PmObject, Severity, String, int, Object...)}
   */
  public static PmMessage makeMsgOneMany(PmObject pm, Severity severity, String keybase, int number, Object... resArgs) {
    return PmMessageApi.addMsgOneMany(pm, severity, keybase, number, resArgs);
  }

  /**
   * Provides a success message when a string resource for the given key is
   * defined.
   *
   * @param key
   *          The resource key to be searched for.
   * @param msgArgs
   *          Optional message arguments.
   *          
   * @deprecated use {@link PmMessageApi#addOptionalInfoMsg(PmObject, String, Object...)}
   */
  public static void makeOptionalInfoMsg(PmObject pm, String key, Object... msgArgs) {
    PmMessageApi.addOptionalInfoMsg(pm, key, msgArgs);
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
   * @deprecated use {@link PmMessageApi#addRequiredMessage(PmAttr)}
   */
  public static PmResourceData makeRequiredWarning(PmAttr<?> pm) {
    return PmMessageApi.addRequiredMessage(pm);
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
   * @deprecated use {@link PmMessageApi#getErrors(PmObject)}
   */
  public static List<PmMessage> getPmErrors(PmObject pm) {
    return PmMessageApi.getErrors(pm);
  }

  /**
   * @return Warning messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   * 
   * @deprecated use {@link PmMessageApi#getWarnings(PmObject)}
   */
  public static List<PmMessage> getPmWarnings(PmObject pm) {
    return PmMessageApi.getWarnings(pm);
  }

  /**
   * @return Info messages that are related to this presentation model.<br>
   *         In case of no messages an empty collection.
   *         
   * @deprecated use {@link PmMessageApi#getInfos(PmObject)}
   */
  public static List<PmMessage> getPmInfos(PmObject pm) {
    return PmMessageApi.getInfos(pm);
  }

  /**
   * @param pm
   *          The PM to get the most severe message for.
   * @return The most severe message for the given PM or <code>null</code> if
   *         there is no message for the given PM.
   *         
   * @deprecated use {@link PmMessageApi#findMostSevereMessage(PmObject)}
   */
  public static PmMessage findMostSevereMessage(PmObject pm) {
    return PmMessageApi.findMostSevereMessage(pm);
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
