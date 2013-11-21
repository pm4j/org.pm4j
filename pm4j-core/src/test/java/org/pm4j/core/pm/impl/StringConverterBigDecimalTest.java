package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.common.converter.string.StringConverterBigDecimal;
import org.pm4j.core.pm.PmConversation;

public class StringConverterBigDecimalTest {

  private PmConversation conversation = new PmConversationImpl();

  private PmAttrBigDecimalImpl someAttr = new PmAttrBigDecimalImpl(conversation);

  private StringConverterBigDecimal converterBigDecimal = new StringConverterBigDecimal();

  @Before
  public void setUp() {
    conversation.setPmLocale(Locale.ENGLISH);
    someAttr.setValue(new BigDecimal(0.5));
  }

  @Test
  public void testWithNonBigDecimalAttr() {
    String string = converterBigDecimal.valueToString(someAttr.getConverterCtxt(), someAttr.getValue());
    assertEquals("0.5", string);
  }

  @Test
  @Ignore("FIXME: GERMAN properties resource not found.")
  public void testWithNonBigDecimalAttrGerman() {
    conversation.setPmLocale(Locale.GERMAN);
    assertEquals("0,5", converterBigDecimal.valueToString(someAttr.getConverterCtxt(), someAttr.getValue()));
  }
}
