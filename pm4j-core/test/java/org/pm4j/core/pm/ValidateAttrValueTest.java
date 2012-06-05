package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.VALIDATE;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrIntegerCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmUtil;

public class ValidateAttrValueTest {

  public static class MyPmClass extends PmConversationImpl {
    public static class NestedPm extends PmElementImpl {
      public NestedPm(PmObject pmParent) { super(pmParent); }

      @PmAttrIntegerCfg(maxValue=10)
      public final PmAttrInteger j = new PmAttrIntegerImpl(this);
    }

    @PmAttrCfg(required=true)
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);

    public final NestedPm nestedPm = new NestedPm(this);

    @PmCommandCfg(beforeDo=VALIDATE)
    public final PmCommand cmdWithValidation = new PmCommandImpl(this);
  }

  @Test
  public void testValidation() {
    MyPmClass o = new MyPmClass();

    o.i.setValueAsString("1");
    o.cmdWithValidation.doIt();
    assertTrue(PmUtil.hasValidAttributes(o));
    assertEquals(0, PmMessageUtil.getPmErrors(o).size());

    o.i.setValueAsString("abc");
    assertEquals(1, PmMessageUtil.getSubTreeMessages(o.getPmConversation(), Severity.ERROR).size());
    PmCommand.CommandState cmdState = o.cmdWithValidation.doIt().getCommandState();
    assertEquals(CommandState.BEFORE_DO_RETURNED_FALSE, cmdState);
    assertEquals("abc", o.i.getValueAsString());
    assertEquals(1, PmMessageUtil.getSubTreeMessages(o.getPmConversation(), Severity.ERROR).size());
    assertFalse(PmUtil.hasValidAttributes(o));
    assertEquals(1, PmMessageUtil.getPmErrors(o).size());
    PmMessageUtil.clearSubTreeMessages(o);

    o.i.setValueAsString("");
    o.cmdWithValidation.doIt();
    assertEquals(null, o.i.getValueAsString());
    assertEquals(1, PmMessageUtil.getPmErrors(o).size());
    PmMessageUtil.clearSubTreeMessages(o);

    o.i.setValueAsString("12");
    o.cmdWithValidation.doIt();
    assertEquals("12", o.i.getValueAsString());
    assertTrue(PmUtil.hasValidAttributes(o));
    assertEquals(0, PmMessageUtil.getPmErrors(o).size());
  }

  @Test
  @Ignore("FIXME olaf: this fails sometimes within the test suite.")
  public void testValidateAttrOfNestedElement() {
    MyPmClass o = new MyPmClass();

    o.i.setValue(123);
    o.nestedPm.j.setValue(11);
    o.cmdWithValidation.doIt();

    assertEquals(new Integer(11), o.nestedPm.j.getValue());
    assertFalse(o.nestedPm.j.isPmValid());
    assertTrue(o.i.isPmValid());

    o.clearPmInvalidValues();

    // FIXME olaf: this fails sometimes within the test suite. (because of unresolved timing issues ?)
    assertTrue(o.nestedPm.j.isPmValid());
    assertEquals(new Integer(11), o.nestedPm.j.getValue());
  }

  @Test
  public void testValidatingCommandClearsOtherMessages() {
    MyPmClass o = new MyPmClass();

    PmMessageUtil.makeMsg(o, Severity.INFO, "hello");
    assertEquals("A non attribute related message exists.", 1, o.getPmMessages().size());

    o.cmdWithValidation.doIt();
    System.out.println(o.getPmMessages());
    assertEquals("A failed validating command execution clears other messages and provides attribute related messages", 1, o.getPmMessages().size());

    PmMessageUtil.makeMsg(o, Severity.INFO, "hello");

    o.i.setValue(3);
    o.cmdWithValidation.doIt();
    assertEquals("A successful validating command execution clears all old messages.", 0, o.getPmMessages().size());
  }
}
