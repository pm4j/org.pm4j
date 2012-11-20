package org.pm4j.core.pm;

import java.util.Locale;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.testdomains.user.User;
import org.pm4j.testdomains.user.UserPm;

public class PmMessageTest extends TestCase {

  public void testMessageTitle() {
    PmConversation session = new PmConversationImpl(UserPm.class);
    session.setPmLocale(Locale.GERMAN);
    UserPm userPm = PmFactoryApi.getPmForBean(session, new User("X"));
    PmMessage m = PmMessageUtil.makeMsg(userPm, Severity.ERROR, "userPm.testmsg", "Das ist ein Test.");

    assertEquals("Testnachricht fuer Nutzer X: Das ist ein Test.", m.getTitle());
    assertTrue(! session.isPmValid());
  }
}