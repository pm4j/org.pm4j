package org.pm4j.core.pm.impl.message;

import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.impl.PmConversationImpl;

/**
 * An interface for message business logic that may be configured by calling
 * {@link PmConversationImpl#setAddPmMessageCallback(AddPmMessageCallback)}.
 * <p>
 * A common use case is some logic that re-arranges PM message severity based on
 * a workflow state.
 */
public interface AddPmMessageCallback {

  /**
   * Gets called in method {@link PmConversationImpl#addPmMessage(PmMessage)},
   * allows to customize {@link PmMessage} handling.
   *
   * @param message
   *          The message that gets registered by calling
   *          {@link PmConversationImpl#addPmMessage(PmMessage)}.
   * @return The message again (or a clone of it). May provide domain logic
   *         specific adjustments.
   */
  PmMessage beforeAddMessage(PmMessage message);
}