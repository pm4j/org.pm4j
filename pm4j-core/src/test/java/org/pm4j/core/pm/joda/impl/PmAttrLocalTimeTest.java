package org.pm4j.core.pm.joda.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pm4j.tools.test.PmAssert.assertMessage;
import static org.pm4j.tools.test.PmAssert.setValueAsString;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.converter.value.joda.LocalTimeTimeZoneConverter;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.joda.PmAttrLocalTime;
import org.pm4j.tools.test.PmAssert;

/**
 * Test for {@link PmAttrLocalTime}.
 *
 * @author oboede
 */
public class PmAttrLocalTimeTest {

  private TestPm testPm = new TestPm();

  @Before
  public void setUp() {
    testPm.setPmLocale(Locale.ENGLISH);
    testPm.setPmTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void conversionError() {
    testPm.time.setValueAsString("10:00a");
    assertFalse(testPm.time.isPmValid());
    assertMessage("The value of the field \"local time\" cannot be interpreted. Please use the format \"HH:mm\".",
                  Severity.ERROR, testPm.time);
  }

  @Test
  public void parseSuccess() {
    setValueAsString(testPm.time, "10:00");
  }

  @Test
  public void testBeanValueConversion() {
    LocalTime localDate = new LocalTime(10, 0);
    PmAssert.setValue(testPm.time, localDate);
    assertTrue(testPm.time.isPmValid());
    assertEquals("10:00", testPm.time.getValueAsString());
    assertEquals(localDate, testPm.time.getBackingValue());
  }

  @Test
  public void testWithTimeZone() {
    PmAttrLocalTimeImpl t = testPm.timeWithTzConverter;
    t.setBackingValue(new LocalTime(18, 00));
    assertEquals("18:00", t.getValueAsString());

    testPm.setPmTimeZone(TimeZone.getTimeZone("Etc/GMT-1"));
    assertEquals("19:00", t.getValueAsString());

    setValueAsString(t, "21:00");
    assertEquals("20:00", t.getBackingValue().toString("HH:mm"));
  }

  public static class TestPm extends PmConversationImpl {

    @PmTitleCfg(title="local time")
    public PmAttrLocalTimeImpl time = new PmAttrLocalTimeImpl(this);

    @PmTitleCfg(title="local time with time zone")
    @PmAttrCfg(valueConverter = LocalTimeTimeZoneConverter.class)
    public PmAttrLocalTimeImpl timeWithTzConverter = new PmAttrLocalTimeImpl(this);
  }
}
