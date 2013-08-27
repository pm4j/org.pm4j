package org.pm4j.core.joda.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Locale;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

public class PmAttrLocalTimeImpl2Test {

  private PmConversation conversation;

  @Before
  public void setUp() {
    conversation = new PmConversationImpl();
    conversation.setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void conversionError() {
    PmAttrLocalTimeImpl2 timePm = new PmAttrLocalTimeImpl2(conversation);
    timePm.setValueAsString("20.Apr.2012");
    assertFalse(timePm.isPmValid());
    PmAssert.assertSingleErrorMessage(timePm,
        "The value of the field \"pmAttrLocalTimeImpl2\" cannot be interpreted. Please use the format \"HH:mm:ss\".");
  }

  @Test
  public void parseSuccess() {
    PmAttrLocalTimeImpl2 timePm = new PmAttrLocalTimeImpl2(conversation);
    timePm.setValueAsString("15:26:48");
    assertTrue(timePm.isPmValid());
    assertEquals(new LocalTime(15, 26, 48), timePm.getBackingValue());
    assertEquals("15:26:48", timePm.getValueAsString());
    assertEquals(new LocalTime(15, 26, 48), timePm.getBackingValue());
  }

  @Test
  public void testLocalTimeComparison() {
    PmAttrLocalTimeImpl2 timePm1 = new PmAttrLocalTimeImpl2(conversation);
    timePm1.setValue(new LocalTime(12, 5, 29));
    PmAttrLocalTimeImpl2 timePm2 = new PmAttrLocalTimeImpl2(conversation);
    timePm2.setValue(new LocalTime(12, 6, 11));
    PmAttrLocalTimeImpl2 timePm3 = new PmAttrLocalTimeImpl2(conversation);
    timePm3.setValue(new LocalTime(13, 1, 2));
    PmAttrLocalTimeImpl2 timePm4 = new PmAttrLocalTimeImpl2(conversation);
    timePm4.setValue(new LocalTime(12, 5, 29));

    // x < y
    assertEquals(timePm1.compareTo(timePm2), -1);
    assertEquals(timePm2.compareTo(timePm3), -1);
    assertEquals(timePm1.compareTo(timePm3), -1);

    // x > y
    assertEquals(timePm2.compareTo(timePm1), 1);
    assertEquals(timePm3.compareTo(timePm2), 1);
    assertEquals(timePm3.compareTo(timePm1), 1);

    // x = y
    assertEquals(timePm1.compareTo(timePm1), 0);
    assertEquals(timePm1.compareTo(timePm4), 0);
  }

}
