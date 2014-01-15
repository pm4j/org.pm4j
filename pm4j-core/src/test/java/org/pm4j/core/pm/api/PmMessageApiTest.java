package org.pm4j.core.pm.api;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmMessageApiTest {

  private TestPm testPm = new TestPm();

  @Before
  public void setUp() {
    testPm.getPmConversation().setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void testFindMostSevereMessage() {
    PmConversationImpl myPm = new PmConversationImpl();

    PmMessage infoMsg = PmMessageApi.addMessage(myPm, Severity.INFO, "infoMsgKey");
    PmMessage warnMsg = PmMessageApi.addMessage(myPm, Severity.WARN, "warnMsgKey");
    PmMessage errorMsg = PmMessageApi.addMessage(myPm, Severity.ERROR, "errorMsgKey");

    assertEquals(errorMsg, PmMessageUtil.findMostSevereMessage(myPm));

    myPm.clearPmMessages(null, Severity.ERROR);
    assertEquals(warnMsg, PmMessageUtil.findMostSevereMessage(myPm));

    myPm.clearPmMessages(null, Severity.WARN);
    assertEquals(infoMsg, PmMessageUtil.findMostSevereMessage(myPm));

    myPm.clearPmMessages(null, null);
    Assert.assertNull(PmMessageUtil.findMostSevereMessage(myPm));
  }

  /**
   * Tests the logic that is implemented in {@link PmMessageApi#addRequiredMessage(org.pm4j.core.pm.PmAttr)}
   */
  @Test
  public void testRequiredDefaultMessage() {
    testPm.pmValidate();
    Assert.assertEquals("Please enter a value into \"Attr 1\".", PmMessageApi.getMessages(testPm.attrWithDefaultRequiredMessage).get(0).getTitle());
  }

  /**
   * Tests the logic that is implemented in {@link PmMessageApi#addRequiredMessage(org.pm4j.core.pm.PmAttr)}
   */
  @Test
  public void testRequiredResourceDefinedSpecialMessage() {
    testPm.pmValidate();
    Assert.assertEquals("The attribute Attr 2 is really required ;-)", PmMessageApi.getMessages(testPm.attrWithIndividualRequiredMessage).get(0).getTitle());
  }


  /** A PM used within this test. */
  public static class TestPm extends PmConversationImpl {
    @PmAttrCfg(required=true)
    public final PmAttrString attrWithDefaultRequiredMessage = new PmAttrStringImpl(this);

    /** Has a special required message defined in the Resource.properties file.  */
    @PmAttrCfg(required=true)
    public final PmAttrString attrWithIndividualRequiredMessage = new PmAttrStringImpl(this);
  }

}
