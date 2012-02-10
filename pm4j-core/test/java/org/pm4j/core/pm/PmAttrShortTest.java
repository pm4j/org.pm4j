package org.pm4j.core.pm;

import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;
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

    Assert.assertEquals("The assigned value should be the current one.", assignedValue, myPm.shortAttr.getValue());
    Assert.assertEquals("The assigned value should also appear as string.", assignedValue.toString(), myPm.shortAttr.getValueAsString());
  }

  static class MyPm extends PmConversationImpl {
    public final PmAttrShort shortAttr = new PmAttrShortImpl(this);
  }

}
