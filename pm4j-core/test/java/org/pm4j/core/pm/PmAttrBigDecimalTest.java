package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrBigDecimalTest {

  @Test
  public void testValueAccess() {
    MyPm myPm = new MyPm();

    assertNull("Initial value should be null", myPm.bigAttr.getValue());
    assertNull("Initial value as string should be null", myPm.bigAttr.getValueAsString());

    BigDecimal assignedValue = new BigDecimal("123");
    myPm.bigAttr.setValue(assignedValue);

    assertEquals("The assigned value should be the current one.", assignedValue, myPm.bigAttr.getValue());
    assertEquals("The assigned value should also appear as string.", assignedValue.toString(), myPm.bigAttr.getValueAsString());
  }

  @Test
  public void testMaxLen() {
    MyPm myPm = new MyPm();

    assertEquals(6, myPm.bigAttr.getMaxLen());
  }

  @Test
  public void testDefaultStringFormat() {
    MyPm myPm = new MyPm();

    assertEquals("An un-set value provides a null.", null, myPm.bigAttr.getValueAsString());
    myPm.bigAttr.setValueAsString("0");

    assertEquals("Default format for zero.", "0", myPm.bigAttr.getValueAsString());

    myPm.bigAttr.setValueAsString("0.123");
    assertEquals("0.123", myPm.bigAttr.getValueAsString());

  }

  @Test
  public void testFormatted() {
    MyPm myPm = new MyPm();

    assertEquals("An un-set value provides a null.", null, myPm.bigFormatted.getValueAsString());
    myPm.bigFormatted.setValueAsString("0");

    assertEquals("Default format for zero.", "0", myPm.bigFormatted.getValueAsString());

    myPm.bigFormatted.setValueAsString("0.123");
    assertEquals("0.123", myPm.bigFormatted.getValueAsString());

  }



  static class MyPm extends PmConversationImpl {
    @PmAttrCfg(maxLen=6)
    public final PmAttrBigDecimal bigAttr = new PmAttrBigDecimalImpl(this);
    @PmAttrCfg(formatResKey="")
    public final PmAttrBigDecimal bigFormatted = new PmAttrBigDecimalImpl(this);
  }

}
