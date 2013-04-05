package org.pm4j.core.pm;

import static org.pm4j.tools.test.PmAssert.doIt;

import org.junit.Test;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

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
