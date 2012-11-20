package org.pm4j.core.pm.api;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmMessageUtilTest {

  @Test
  public void testFindMostSevereMessage() {
    PmConversationImpl myPm = new PmConversationImpl();

    PmMessage infoMsg = PmMessageUtil.makeMsg(myPm, Severity.INFO, "infoMsgKey");
    PmMessage warnMsg = PmMessageUtil.makeMsg(myPm, Severity.WARN, "warnMsgKey");
    PmMessage errorMsg = PmMessageUtil.makeMsg(myPm, Severity.ERROR, "errorMsgKey");

    assertEquals(errorMsg, PmMessageUtil.findMostSevereMessage(myPm));

    myPm.clearPmMessages(null, Severity.ERROR);
    assertEquals(warnMsg, PmMessageUtil.findMostSevereMessage(myPm));

    myPm.clearPmMessages(null, Severity.WARN);
    assertEquals(infoMsg, PmMessageUtil.findMostSevereMessage(myPm));

    myPm.clearPmMessages(null, null);
    Assert.assertNull(PmMessageUtil.findMostSevereMessage(myPm));
  }
}
