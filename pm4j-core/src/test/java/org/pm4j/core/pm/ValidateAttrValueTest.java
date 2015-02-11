package org.pm4j.core.pm;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.Validate;
import org.pm4j.core.pm.annotation.PmAttrIntegerCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.VALIDATE;
import static org.pm4j.tools.test._PmAssert.assertMessageText;
import static org.pm4j.tools.test._PmAssert.assertNoMessagesInSubTree;

public class ValidateAttrValueTest {

  private MyPmClass pm;

  @Before
  public void setUp() {
    pm = new MyPmClass();
    pm.setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void testClearMessagesShouldClearInvalidValuesToo() {
    pm.i.setValueAsString("xyz");
    assertFalse("An integer attribute can't accept letters.", pm.i.isPmValid());
    assertEquals("The invalid value should be available as 'string' value for display purposes.", "xyz", pm.i.getValueAsString());

    PmMessageApi.clearPmTreeMessages(pm.i);
    assertTrue("After clearing all messages the attribute should be valid again.", pm.i.isPmValid());
    assertEquals("After clearing the valueAsString should be null again.", null, pm.i.getValueAsString());
  }

  @Test
  public void testValidation() {
    pm.i.setValueAsString("1");
    pm.cmdWithValidation.doIt();
    assertTrue(PmValidationApi.hasValidAttributes(pm));
    assertEquals(0, PmMessageApi.getMessages(pm, Severity.ERROR).size());

    pm.i.setValueAsString("abc");
    assertEquals(1, PmMessageApi.getPmTreeMessages(pm.getPmConversation(), Severity.ERROR).size());
    PmCommand.CommandState cmdState = pm.cmdWithValidation.doIt().getCommandState();
    assertEquals(CommandState.BEFORE_DO_RETURNED_FALSE, cmdState);
    assertEquals("abc", pm.i.getValueAsString());
    assertEquals(1, PmMessageApi.getPmTreeMessages(pm.getPmConversation(), Severity.ERROR).size());
    assertFalse(PmValidationApi.hasValidAttributes(pm));
    assertEquals(1, PmMessageApi.getMessages(pm, Severity.ERROR).size());
    PmMessageApi.clearPmTreeMessages(pm);

    pm.i.setValueAsString("");
    pm.cmdWithValidation.doIt();
    assertEquals(null, pm.i.getValueAsString());
    assertEquals(1, PmMessageApi.getMessages(pm, Severity.ERROR).size());
    PmMessageApi.clearPmTreeMessages(pm);

    pm.i.setValueAsString("12");
    pm.cmdWithValidation.doIt();
    assertEquals("12", pm.i.getValueAsString());
    assertTrue(PmValidationApi.hasValidAttributes(pm));
    assertEquals(0, PmMessageApi.getMessages(pm, Severity.ERROR).size());
  }

  @Test
  @Ignore("FIXME olaf: this fails sometimes within the test suite.")
  public void testValidateAttrOfNestedElement() {
    pm.i.setValue(123);
    pm.nestedPm.j.setValue(11);
    pm.cmdWithValidation.doIt();

    assertEquals(new Integer(11), pm.nestedPm.j.getValue());
    assertFalse(pm.nestedPm.j.isPmValid());
    assertTrue(pm.i.isPmValid());

    pm.clearPmInvalidValues();

    // FIXME olaf: this fails sometimes within the test suite. (because of unresolved timing issues ?)
    assertTrue(pm.nestedPm.j.isPmValid());
    assertEquals(new Integer(11), pm.nestedPm.j.getValue());
  }

  @Test
  public void testValidatingCommandClearsOtherMessages() {
    PmMessageApi.addMessage(pm, Severity.INFO, "hello");
    assertEquals("A non attribute related message exists.", 1, pm.getPmMessages().size());

    pm.cmdWithValidation.doIt();
    System.out.println(pm.getPmMessages());
    assertEquals("A failed validating command execution clears other messages and provides attribute related messages", 1, pm.getPmMessages().size());

    PmMessageApi.addMessage(pm, Severity.INFO, "hello");

    pm.i.setValue(3);
    pm.cmdWithValidation.doIt();
    assertEquals("A successful validating command execution clears all old messages.", 0, pm.getPmMessages().size());
  }

  @Test
  public void testValidateBeforeSetValue() {
    pm.validateBeforeSetValueAttr.setValue(6);
    assertMessageText(pm.validateBeforeSetValueAttr, "Please enter a number not more than 5 in field \"BeforeSetValueValidatingAttr\".");
    assertEquals("Before set value validation prevents setting the backing value.",
        null, ((PmAttrIntegerImpl)pm.validateBeforeSetValueAttr).getBackingValue());
    assertEquals("The invalid value will still be presented in the UI.",
                  new Integer(6), pm.validateBeforeSetValueAttr.getValue());

    pm.validateBeforeSetValueAttr.setValue(4);
    assertNoMessagesInSubTree("Setting a valid value clears the error message.", pm.validateBeforeSetValueAttr);
  }

  @Test
  public void testValidateBeforeSetValueDeprecatedVersion() {
    pm.validateBeforeSetValueAttrDeprecatedVersion.setValue(6);
    assertMessageText(pm.validateBeforeSetValueAttrDeprecatedVersion, "Please enter a number not more than 5 in field \"BeforeSetValueValidatingAttr\".");
    assertEquals("Before set value validation prevents setting the backing value.",
        null, ((PmAttrIntegerImpl)pm.validateBeforeSetValueAttrDeprecatedVersion).getBackingValue());
    assertEquals("The invalid value will still be presented in the UI.",
                  new Integer(6), pm.validateBeforeSetValueAttrDeprecatedVersion.getValue());

    pm.validateBeforeSetValueAttrDeprecatedVersion.setValue(4);
    assertNoMessagesInSubTree("Setting a valid value clears the error message.", pm.validateBeforeSetValueAttrDeprecatedVersion);
  }

  @Test
  public void testValidateAfterSetValue() {
    pm.validateAfterSetValueAttr.setValue(6);
    assertMessageText(pm.validateAfterSetValueAttr, "Please enter a number not more than 5 in field \"AfterSetValueValidatingAttr\".");
    assertEquals("Before set value validation NOT prevents setting the backing value.",
        new Integer(6), ((PmAttrIntegerImpl)pm.validateAfterSetValueAttr).getBackingValue());
    assertEquals("After set-value validation does not prevent setting the value.",
        new Integer(6), pm.validateAfterSetValueAttr.getValue());

    pm.validateAfterSetValueAttr.setValue(4);
    assertNoMessagesInSubTree("Setting a valid value clears the error message.", pm.validateAfterSetValueAttr);
  }


  /**
   * The testee.
   */
  public static class MyPmClass extends PmConversationImpl {
    public static class NestedPm extends PmObjectBase {
      public NestedPm(PmObject pmParent) { super(pmParent); }

      @PmAttrIntegerCfg(maxValue=10)
      public final PmAttrInteger j = new PmAttrIntegerImpl(this);
    }

    @PmAttrCfg(required=true)
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);

    @PmAttrIntegerCfg(maxValue=5)
    @PmAttrCfg(validate=Validate.BEFORE_SET)
    public final PmAttrInteger validateBeforeSetValueAttr = new PmAttrIntegerImpl(this);

    @PmAttrIntegerCfg(maxValue=5)
    @PmAttrCfg(validate=Validate.BEFORE_SET)
    public final PmAttrInteger validateBeforeSetValueAttrDeprecatedVersion = new PmAttrIntegerImpl(this) {
      @Override
      protected boolean isValidatingOnSetPmValue() {
        return true;
      };
    };

    @PmAttrIntegerCfg(maxValue=5)
    @PmAttrCfg(validate=Validate.AFTER_SET)
    public final PmAttrInteger validateAfterSetValueAttr = new PmAttrIntegerImpl(this);


    public final NestedPm nestedPm = new NestedPm(this);

    @PmCommandCfg(beforeDo=VALIDATE)
    public final PmCommand cmdWithValidation = new PmCommandImpl(this);
  }

}
