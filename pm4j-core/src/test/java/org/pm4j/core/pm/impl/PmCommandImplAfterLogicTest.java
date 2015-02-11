package org.pm4j.core.pm.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.*;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.AFTER_DO;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.navi.NaviLink;

/**
 * Tests for the command logic that should happen before it gets executed.
 *
 * @author olaf boede
 */
public class PmCommandImplAfterLogicTest {

  private PmConversationImpl conversationPm;
  private MyTestPm myTestPm;

  @Before
  public void setUp() {
    conversationPm = new PmConversationImpl();
    myTestPm = new MyTestPm(conversationPm);

    assertEquals("The test starts without validation errors.",
        0, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );

    // TODO olaf: enhance the default exception handler!
    conversationPm.setPmExceptionHandler(new PmExceptionHandlerImpl() {
      @Override
      public NaviLink onException(PmObject pmObject, Throwable throwable, boolean inNaviContext) {
        PmMessageUtil.makeExceptionMsg(pmObject, Severity.ERROR, throwable);
        return null;
      }
    });
  }

  @Test
  public void testACommandTriggeringValidation() {
    myTestPm.s.setValue("hello");

    myTestPm.cmdThatTriggersValidation.doIt();

    assertEquals("Calling a validating command in a valid context should not generate errors.",
            0, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );

    assertTrue("The doItImpl() method should have been called.", myTestPm.doItImplHasBeenExecuted);
    assertTrue(myTestPm.isPmValueChanged());
  }
  
  @Test
  public void testACommandTriggeringValidationAndReset() {
    myTestPm.s.setValue("hello");

    myTestPm.cmdThatTriggersValidationAndReset.doIt();

    assertEquals("Calling a validating command in a valid context should not generate errors.",
            0, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );

    assertTrue("The doItImpl() method should have been called.", myTestPm.doItImplHasBeenExecuted);
    assertFalse(myTestPm.isPmValueChanged());
  }

  /**
   * A PM with some command variations to test.
   */
  public static class MyTestPm extends PmObjectBase {

    @PmAttrCfg(required=true)
    public final PmAttrString s = new PmAttrStringImpl(this);

    @PmCommandCfg(beforeDo=VALIDATE, afterDo=AFTER_DO.DO_NOTHING)
    public final PmCommand cmdThatTriggersValidation = new ExecReportingCmdPm();
    
    @PmCommandCfg(beforeDo=VALIDATE) 
    // reset value changed state is the default for afterDo if beforeDo=VALIDATE
    public final PmCommand cmdThatTriggersValidationAndReset = new ExecReportingCmdPm();

    public final PmCommand cmdWithDefaultAfterLogic = new ExecReportingCmdPm();

    public MyTestPm(PmConversation conversationPm) {
      super(conversationPm);
    }

    // exec observation helper code

    public boolean doItImplHasBeenExecuted;

    class ExecReportingCmdPm extends PmCommandImpl {

      public ExecReportingCmdPm() {
        super(MyTestPm.this);
      }

      @Override
      protected void doItImpl()  {
        doItImplHasBeenExecuted = true;
      }
    }
  }
}
