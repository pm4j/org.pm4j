package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.impl.PmAttrShortImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrShortTest {

  private MyPm myPm = new MyPm();

  @Before
  public void setUp() {
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

    assertEquals("There should be a string conversion error", 1, PmMessageApi.getErrors(myPm.shortAttr).size());
    assertEquals("The error should be a number conversion error.",
                  "Unable to convert the entered string to a numeric value in field \"abc\".", PmMessageApi.findMostSevereMessage(myPm.shortAttr).getTitle());
  }

  @Test
  public void testValueType() {
    Class<?> t = myPm.shortAttr.getValueType();
    assertEquals(Short.class, t);
  }


  static class MyPm extends PmConversationImpl {
    public final PmAttrShort shortAttr = new PmAttrShortImpl(this);
  }

}
