package org.pm4j.core.pm.impl.converter;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmConverterBigDecimalTest {

  private PmConversation conversation = new PmConversationImpl();
  private PmAttr<BigDecimal> someAttr = new PmAttrImpl<BigDecimal>(conversation);
  private PmConverterBigDecimal converterBigDecimal = new PmConverterBigDecimal();

  @Before
  public void setUp() {
    conversation.setPmLocale(Locale.ENGLISH);
    someAttr.setValue(new BigDecimal(0.5));
  }

  @Test
  public void testWithNonBigDecimalAttr() {
    String string = converterBigDecimal.valueToString(someAttr, someAttr.getValue());
    assertEquals("0.50", string);
  }

  @Test
  public void testWithNonBigDecimalAttrGerman() {
    conversation.setPmLocale(Locale.GERMAN);
    assertEquals("0,50", converterBigDecimal.valueToString(someAttr, someAttr.getValue()));
  }
}
