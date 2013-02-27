package org.pm4j.core.pm.impl.converter;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmConverterBigDecimalTest {

  private PmConversation conversation = new PmConversationImpl();
  
  private PmAttrBigDecimal someAttr = new PmAttrBigDecimalImpl(conversation);
  
  private PmConverterBigDecimal converterBigDecimal = new PmConverterBigDecimal();

  @Before
  public void setUp() {
    conversation.setPmLocale(Locale.ENGLISH);
    someAttr.setValue(new BigDecimal(0.5));
  }

  @Test
  public void testWithNonBigDecimalAttr() {
    String string = converterBigDecimal.valueToString(someAttr, someAttr.getValue());
    assertEquals("0.5", string);
  }

  @Test
  @Ignore("FIXME: GERMAN properties resource not found.")
  public void testWithNonBigDecimalAttrGerman() {
    conversation.setPmLocale(Locale.GERMAN);
    assertEquals("0,5", converterBigDecimal.valueToString(someAttr, someAttr.getValue()));
  }
}
