package org.pm4j.core.pm.impl.converter;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmConverterBigDecimalTest {
  @Test
  public void testWithNonBigDecimalAttr() {
    PmConverterBigDecimal converterBigDecimal = new PmConverterBigDecimal();
    PmAttr<BigDecimal> someAttr = new PmAttrImpl<BigDecimal>(new PmConversationImpl());
    someAttr.setValue(new BigDecimal(0.5));
    String string = converterBigDecimal.valueToString(someAttr, someAttr.getValue());
    assertEquals("0.50", string);
  }
}
