package org.pm4j.core.pm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmMessageTest {

  @Test
  public void testMessageTitle() {
    PmConversation conversation = new PmConversationImpl();
    PmMessage m = PmMessageApi.addMessage(conversation, Severity.ERROR, "pmMessageTest.testmsg", "Message parameter");

    assertEquals("A test message with parameter: Message parameter", m.getTitle());
    assertTrue(! conversation.isPmValid());
  }

  @Test
  public void testMessageComparator() {
    PmConversation conversation = new PmConversationImpl();
    PmMessageApi.addMessage(conversation, Severity.INFO, "pmMessageTest.testmsg", "Info message parameter");
    PmMessageApi.addMessage(conversation, Severity.WARN, "pmMessageTest.testmsg", "Warning message parameter");
    PmMessageApi.addMessage(conversation, Severity.ERROR, "pmMessageTest.testmsg", "Error bbbbbbbbb");
    PmMessage m = PmMessageApi.addMessage(conversation, Severity.ERROR, "pmMessageTest.testmsg", "Error aaaaaaaaa");

    assertSame(PmMessageUtil.findMostSevereMessage(conversation), m);
    assertSame(PmMessageUtil.findMostSevereMessage(conversation, Severity.INFO), m);
    assertSame(PmMessageUtil.findMostSevereMessage(conversation, Severity.WARN), m);
    assertSame(PmMessageUtil.findMostSevereMessage(conversation, Severity.ERROR), m);
  }

  @Test
  public void testMessageComparatorMinimum() {
    PmConversation conversation = new PmConversationImpl();
    PmMessageApi.addMessage(conversation, Severity.INFO, "pmMessageTest.testmsg", "Info message parameter");
    PmMessageApi.addMessage(conversation, Severity.WARN, "pmMessageTest.testmsg", "Warning bbbbbbbbb");
    PmMessage m = PmMessageApi.addMessage(conversation, Severity.WARN, "pmMessageTest.testmsg", "Warning aaaaaaaaa");

    assertSame(PmMessageUtil.findMostSevereMessage(conversation), m);
    assertSame(PmMessageUtil.findMostSevereMessage(conversation, Severity.INFO), m);
    assertSame(PmMessageUtil.findMostSevereMessage(conversation, Severity.WARN), m);
    assertNull(PmMessageUtil.findMostSevereMessage(conversation, Severity.ERROR));
  }
}
