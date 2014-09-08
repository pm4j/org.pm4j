package org.pm4j.core.pm.impl.message;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.impl.PmValidationMessage;
import org.pm4j.core.pm.impl.converter.PmConverterErrorMessage;

/**
 * A base class for add message call-backs.<br>
 * A common use case for this class is some logic that re-arranges PM message severity based
 * on a workflow state.
 * <p>
 * Please implement {@link #beforeAddMessageImpl(PmMessage)} to provide your message logic.<br>
 * You may override {@link #isMessageToHandle(PmMessage)} to control the set of messages to handle
 * by your specific logic.
 *
 * @author Olaf Boede
 */
public abstract class AddPmMessageCallbackBase implements AddPmMessageCallback {

  @Override
  public final PmMessage beforeAddMessage(PmMessage message) {
    return isMessageToHandle(message)
        ? beforeAddMessageImpl(message)
        : message;
  }

  /**
   * Filters the messages.
   * <p>
   * The default implementation forwards messages that are not {@link PmConverterErrorMessage}s
   * to {@link #beforeAddMessageImpl(PmMessage)}.<br>
   * This seems to be ok for most scenarios, because it is hard for domain logic code to handle not convertible
   * values. (E.g. an input of "abc" for a time field).
   *
   * @param message The message to check.
   * @return <code>true</code> if the message should be handled in {@link #beforeAddMessageImpl(PmMessage)}.
   */
  protected boolean isMessageToHandle(PmMessage message) {
    return !(message instanceof PmConverterErrorMessage);
  }

  /**
   * Please override this method to implement your specific message logic.
   * <p>
   * Please notice that a {@link PmValidationMessage} should stay a kind of {@link PmValidationMessage}
   * because it serves as a holder for invalid values.
   *
   * @param message The message to handle.
   * @return The message to add finally to the {@link PmConversation}.<br>
   *   May be the message that was passed (e.g. with re-adjusted severity) or new message.<br>
   *   May also be <code>null</code> if the message should be ignored.
   */
  protected abstract PmMessage beforeAddMessageImpl(PmMessage message);

}
