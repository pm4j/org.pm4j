package org.pm4j.core.pm.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.CLEAR;
import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.DO_NOTHING;
import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.VALIDATE;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.navi.NaviLink;

/**
 * Tests for the command logic that should happen before it gets executed.
 *
 * @author olaf boede
 */
public class PmCommandImplBeforeLogicTest {

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
  public void testACommandTriggeringValidationInInvalidDataConstellation() {
    // call the command without setting the required attribute value
    myTestPm.cmdThatTriggersValidation.doIt();

    assertEquals("Calling a validating command should generate messages for invalid attribute.",
            1, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );

    assertFalse("The doItImpl() method should not be called in case of validation errors.", myTestPm.doItImplHasBeenExecuted);
  }

  @Test
  public void testACommandTriggeringValidationInValidDataConstellation() {
    myTestPm.s.setValue("hello");

    myTestPm.cmdThatTriggersValidation.doIt();

    assertEquals("Calling a validating command in a valid context should not generate errors.",
            0, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );

    assertTrue("The doItImpl() method should have been called.", myTestPm.doItImplHasBeenExecuted);
  }

  @Test
  public void testACommandTheDoesNothingBefore() {
    myTestPm.s.setValue("hello");

    myTestPm.cmdThatDoesNothingBeforeDo.doIt();

    assertEquals("Calling a validating command in a valid context should not generate errors.",
            0, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );
    assertTrue("The doItImpl() method should have been called.", myTestPm.doItImplHasBeenExecuted);

    myTestPm.s.setValue(null);

    assertEquals("Calling a validating command in an invalid context should not generate errors.",
            0, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );
    assertTrue("The doItImpl() method should have been called.", myTestPm.doItImplHasBeenExecuted);
  }

  @Test
  public void testACommandThatClearsValidationMessages() {
    myTestPm.s.setValue(null);
    myTestPm.cmdThatTriggersValidation.doIt();

    assertEquals("The validating command generates an errror.",
            1, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );

    myTestPm.cmdThatJustClearsValidationMessages.doIt();

    assertEquals("A call to the message clearing command clears all errors.",
            0, PmMessageApi.getMessages(conversationPm, Severity.ERROR).size() );
    assertTrue("The doItImpl() method should have been called.", myTestPm.doItImplHasBeenExecuted);
  }

  @Test
  public void testFailingCommand() {
    assertEquals(CommandState.FAILED, myTestPm.cmdThatDoesNothingBeforeDoButFails.doIt().getCommandState());
    assertFalse("The command should be marked as 'invalid'.",
                myTestPm.cmdThatDoesNothingBeforeDoButFails.isPmValid());
    assertEquals("That means: There is an error message for this command.",
                  1, PmMessageApi.getMessages(myTestPm.cmdThatDoesNothingBeforeDoButFails, Severity.ERROR).size());

    assertEquals(CommandState.FAILED, myTestPm.cmdThatDoesNothingBeforeDoButFails.doIt().getCommandState());
    assertEquals("After a second (failing) attempt there should still only be one (new) error message for the command.",
                  1, PmMessageApi.getMessages(myTestPm.cmdThatDoesNothingBeforeDoButFails, Severity.ERROR).size());
  }


  /**
   * A PM with some command variations to test.
   */
  public static class MyTestPm extends PmElementImpl {

    @PmAttrCfg(required=true)
    public final PmAttrString s = new PmAttrStringImpl(this);

    @PmCommandCfg(beforeDo=VALIDATE)
    public final PmCommand cmdThatTriggersValidation = new ExecReportingCmdPm();

    @PmCommandCfg(beforeDo=CLEAR)
    public final PmCommand cmdThatJustClearsValidationMessages = new ExecReportingCmdPm();

    @PmCommandCfg(beforeDo=DO_NOTHING)
    public final PmCommand cmdThatDoesNothingBeforeDo = new ExecReportingCmdPm();

    public final PmCommand cmdWithDefaultBeforeLogic = new ExecReportingCmdPm();

    @PmCommandCfg(beforeDo=DO_NOTHING)
    public final PmCommand cmdThatDoesNothingBeforeDoButFails = new ExecReportingCmdPm() {
      @Override
      protected void doItImpl() throws Exception {
        throw new RuntimeException("This command intentially fails.");
      }
    };

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
      protected void doItImpl() throws Exception {
        doItImplHasBeenExecuted = true;
      }
    }
  }
}
