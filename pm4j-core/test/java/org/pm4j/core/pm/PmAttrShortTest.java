package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmAttrShortImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrShortTest {

  @Test
  public void testValueAccess() {
    MyPm myPm = new MyPm();

    assertNull("Initial value should be null", myPm.shortAttr.getValue());
    assertNull("Initial value as string should be null", myPm.shortAttr.getValueAsString());

    Short assignedValue = new Short((short) 123);
    myPm.shortAttr.setValue(assignedValue);

    assertEquals("The assigned value should be the current one.", assignedValue, myPm.shortAttr.getValue());
    assertEquals("The assigned value should also appear as string.", assignedValue.toString(), myPm.shortAttr.getValueAsString());
  }

  @Test
  public void testInvalidCharacters() {
    MyPm myPm = new MyPm();

    myPm.shortAttr.setValueAsString("abc");

    assertEquals("There should be a string conversion error", 1, PmMessageUtil.getPmErrors(myPm.shortAttr).size());
    assertEquals("The error should be a number conversion error.",
          PmConstants.MSGKEY_VALIDATION_NUMBER_CONVERSION_FROM_STRING_FAILED, PmMessageUtil.findMostSevereMessage(myPm.shortAttr).getMsgKey());
  }

  static class MyPm extends PmConversationImpl {
    public final PmAttrShort shortAttr = new PmAttrShortImpl(this);
  }

}
