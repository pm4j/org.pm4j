package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrEnumTest {

  @Test
  public void testEnumValueChangedState() {
    MyPm myPm = new MyPm();

    assertEquals("The attribute is initially unchanged.", false, myPm.myEnum.isPmValueChanged());

    myPm.myEnum.setValue(MyEnum.A);
    assertEquals("After changing a value, the attribute reports the change.", true, myPm.myEnum.isPmValueChanged());
    assertEquals("After changing a value, the containing element reports the change.", true, myPm.isPmValueChanged());

    myPm.cmdOk.doIt();
    assertEquals("A validating command resets the changed state to unchanged.", false, myPm.myEnum.isPmValueChanged());
    assertEquals("A validating command resets the changed state to unchanged.", false, myPm.isPmValueChanged());

    myPm.myEnum.setValue(MyEnum.B);
    assertEquals("A new value change makes the PM changed again.", true, myPm.myEnum.isPmValueChanged());
    assertEquals("A new value change makes the PM changed again.", true, myPm.isPmValueChanged());

    myPm.myEnum.setValue(MyEnum.A);
    assertEquals("Changing the value back to it's original value will make the field unchanged again.", false, myPm.myEnum.isPmValueChanged());
    assertEquals("Changing the value back to it's original value will make the field unchanged again.", false, myPm.isPmValueChanged());
  }

  @Test
  public void testValueType() {
    Class<?> t = new MyPm().myEnum.getValueType();
    assertEquals(MyEnum.class, t);
    assertTrue(Enum.class.isAssignableFrom(t));
  }

  enum MyEnum { A, B, C };
  public static class MyPm extends PmConversationImpl {
    public final PmAttrEnum<MyEnum> myEnum = new PmAttrEnumImpl<MyEnum>(this, MyEnum.class);
    public final PmCommand cmdOk = new PmCommandImpl(this);
  }

}
