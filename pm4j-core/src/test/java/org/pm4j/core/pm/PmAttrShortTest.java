package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrShortImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

public class PmAttrShortTest {

  private MyPm myPm;

  @Before
  public void setUp() {
    myPm = new MyPm();
    myPm.setPmLocale(Locale.ENGLISH);
  }


  @Test
  public void testValueAccess() {
    assertNull("Initial value should be null", myPm.shortAttr.getValue());
    assertNull("Initial value as string should be null", myPm.shortAttr.getValueAsString());

    Short assignedValue = new Short((short) 123);
    myPm.shortAttr.setValue(assignedValue);

    assertEquals("The assigned value should be the current one.", assignedValue, myPm.shortAttr.getValue());
    assertEquals("The assigned value should also appear as string.", assignedValue.toString(), myPm.shortAttr.getValueAsString());
  }

  @Test
  public void testInvalidCharacters() {
    myPm.shortAttr.setValueAsString("abc");

    assertEquals("There should be a string conversion error", 1, PmMessageApi.getMessages(myPm.shortAttr, Severity.ERROR).size());
    assertEquals("The error should be a number conversion error.",
                  "Unable to convert the entered string to a numeric value in field \"abc\".", PmMessageUtil.findMostSevereMessage(myPm.shortAttr).getTitle());
  }

  @Test
  public void testValueType() {
    Class<?> t = myPm.shortAttr.getValueType();
    assertEquals(Short.class, t);
  }

  @Test
  @Ignore("oboede: deactivated because of intergration problems.")
  public void testMinLength() {

    //Check annotations
    assertEquals(2, myPm.minLen2.getMinLen());

    //Validate too short
    myPm.minLen2.setValue(1);
    PmAssert.validateNotSuccessful(myPm.minLen2, "Please enter at least 2 characters in field \"pmAttrShortTest.MyPm.minLen2\".");

    //Validate correct
    myPm.minLen2.setValue(12);
    PmAssert.validateSuccessful(myPm.minLen2);
  }

  @Test
  @Ignore("oboede: deactivated because of intergration problems.")
  public void testMaxLength() {

    //Check annotations
    assertEquals(6, myPm.maxLen6.getMaxLen());

    //Validate too big
    myPm.maxLen6.setValue(1234567);
    PmAssert.validateNotSuccessful(myPm.maxLen6, "Please enter maximal 6 characters in field \"pmAttrShortTest.MyPm.maxLen6\".");

    //Validate correct
    myPm.maxLen6.setValue(123456);
    PmAssert.validateSuccessful(myPm);
  }


  static class MyPm extends PmConversationImpl {

    public final PmAttrShort shortAttr = new PmAttrShortImpl(this);

    @PmAttrCfg(minLen=2)
    public final PmAttrIntegerImpl minLen2 = new PmAttrIntegerImpl(this);

    @PmAttrCfg(maxLen=6)
    public final PmAttrIntegerImpl maxLen6 = new PmAttrIntegerImpl(this);
  }

}
