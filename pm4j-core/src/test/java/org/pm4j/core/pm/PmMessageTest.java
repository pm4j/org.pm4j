package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmMessageTest extends TestCase {

  public void testMessageTitle() {
    PmConversation conversation = new PmConversationImpl();
    PmMessage m = PmMessageApi.addMessage(conversation, Severity.ERROR, "pmMessageTest.testmsg", "Message parameter");

    assertEquals("A test message with parameter: Message parameter", m.getTitle());
    assertTrue(! conversation.isPmValid());
  }
}