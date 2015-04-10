package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test._PmAssert.doIt;

import org.junit.Test;
import org.pm4j.common.util.CloneUtil;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test._RecordingPmEventListener;

public class PmCommandTest {

  private TestPm testPm = new TestPm();

  @Test
  public void testACommandWithoutAnnotationValidatesItsParentPM() {
    doIt("Command execution fails because of a missing required attribute value.",
        testPm.cmdWithoutAnnotation, CommandState.BEFORE_DO_RETURNED_FALSE);

    testPm.attrToValidate.setValue("abc");
    doIt("Command can be exectuted in a valid PM context.",
        testPm.cmdWithoutAnnotation, CommandState.EXECUTED);
  }

  @Test
  public void testCommandWithBeforeDoNothingAnnotation() {
    doIt("Command can be executed, even if the PM values are not valid.",
        testPm.cmdThatDoesNothingBefore, CommandState.EXECUTED);

    testPm.attrToValidate.setValue("abc");
    doIt("Command can also be exectuted in a valid PM context.",
        testPm.cmdThatDoesNothingBefore, CommandState.EXECUTED);
  }

  @Test
  public void testCommandWithInheritedBeforeDoNothingAnnotation() {
    doIt("Command can be executed, even if the PM values are not valid.",
        testPm.cmdWithInheritedDoNothingBefore, CommandState.EXECUTED);

    testPm.attrToValidate.setValue("abc");
    doIt("Command can also be exectuted in a valid PM context.",
        testPm.cmdWithInheritedDoNothingBefore, CommandState.EXECUTED);
  }

  @Test
  public void testAttachEventListenerToCloneOnly() {
    _RecordingPmEventListener listener = new _RecordingPmEventListener();
    PmCommand cmdClone = CloneUtil.clone(testPm.cmdThatDoesNothingBefore);
    PmEventApi.addPmEventListener(cmdClone, PmEvent.EXEC_COMMAND, listener);

    doIt(testPm.cmdThatDoesNothingBefore);
    assertEquals("A call to the clone template should have an effect to listeners of the clone.",
                 0, listener.getEventCount());

    doIt(cmdClone);
    assertEquals("A call to the clone should have an effect to listeners of the clone.",
                 1, listener.getEventCount());
  }

  @Test
  public void testAttachDecoratorToCloneOnly() {
    RecordingCommandDecorator decorator = new RecordingCommandDecorator();
    PmCommand cmdClone = CloneUtil.clone(testPm.cmdThatDoesNothingBefore);
    cmdClone.addCommandDecorator(decorator);

    doIt(testPm.cmdThatDoesNothingBefore);
    assertEquals("A call to the clone template should have an effect to listeners of the clone.",
                 0, decorator.getBeforeCallCount());

    doIt(cmdClone);
    assertEquals("A call to the clone should have an effect to listeners of the clone.",
                 1, decorator.getBeforeCallCount());
  }



  public static class TestPm extends PmConversationImpl {

    @PmAttrCfg(required=true)
    public final PmAttrString attrToValidate = new PmAttrStringImpl(this);

    public final PmCommand cmdWithoutAnnotation = new PmCommandImpl(this);

    @PmCommandCfg(beforeDo=BEFORE_DO.DO_NOTHING)
    public final PmCommand cmdThatDoesNothingBefore = new PmCommandImpl(this);

    public final PmCommand cmdWithInheritedDoNothingBefore = new CmdClassWithBeforeDoDoNothing(this);
  }

  @PmCommandCfg(beforeDo=BEFORE_DO.DO_NOTHING)
  static class CmdClassWithBeforeDoDoNothing extends PmCommandImpl {

    public CmdClassWithBeforeDoDoNothing(PmObject pmParent) {
      super(pmParent);
    }

  }

}
