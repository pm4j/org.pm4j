package org.pm4j.core.pm.api;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;

public class PmValidationApiTest {

  private PmConversation pmConversation;
  private TestPm testPm;

  @Before
  public void setUp() {
    pmConversation = new PmConversationImpl();
    testPm = new TestPm(pmConversation);
  }

  @Test
  public void testValidateMissingRequiredAttribute() {
    assertFalse(PmValidationApi.validateSubTree(testPm));

    assertFalse(testPm.i.isPmValid());
    assertFalse(testPm.isPmValid());
    assertFalse(pmConversation.isPmValid());
    assertFalse(PmValidationApi.hasValidAttributes(testPm));

    List<PmMessage> messages = PmMessageUtil.getPmErrors(testPm.i);
    assertEquals(1, messages.size());
    assertEquals(PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE, messages.get(0).getMsgKey());
  }

  public static class TestPm extends PmElementImpl {

    @PmAttrCfg(required=true)
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);

    public TestPm(PmObject parentPm) {
      super(parentPm);
    }
  }

}
