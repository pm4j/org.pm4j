package org.pm4j.core.pm.impl.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmConverterNumberTest {

  MyPm myPm;

  @Before
  public void setUp() {
    myPm = new MyPm();
  }

  @Test
  public void testValueToString() {
    String noFractionFormat = "#0";
    assertEquals(new BigDecimal("2.1"), myPm.bdNoDecimalPlaces.getValue());
    assertEquals(noFractionFormat, myPm.bdNoDecimalPlaces.getFormatString());
    try {
      myPm.bdNoDecimalPlaces.getValueAsString();
      fail();
    } catch (PmRuntimeException e) {
      assertTrue(e.getMessage().contains(noFractionFormat));
    }
  }

  static class MyPm extends PmConversationImpl {
    public final PmAttrBigDecimal bdNoDecimalPlaces = new PmAttrBigDecimalImpl(this) {
      // The format and the value does not fit, that's why we use
      // getBackingValueImpl.
      protected BigDecimal getBackingValueImpl() {
        return new BigDecimal("2.1");
      }
    };
  }
}
