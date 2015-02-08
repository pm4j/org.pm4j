package org.pm4j.core.pm.api;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.*;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    List<PmMessage> messages = PmMessageApi.getMessages(testPm.i, Severity.ERROR);
    assertEquals(1, messages.size());
    assertEquals(PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE, messages.get(0).getMsgKey());
  }

  public static class TestPm extends PmObjectBase {

    @PmAttrCfg(required=true)
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);

    public TestPm(PmObject parentPm) {
      super(parentPm);
    }
  }

}
