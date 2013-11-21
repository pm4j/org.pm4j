package org.pm4j.core.joda.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.pm4j.tools.test.PmAssert.assertSingleErrorMessage;
import static org.pm4j.tools.test.PmAssert.setValueAsString;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.converter.value.joda.LocalDateTimeTimeZoneConverter;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test.PmAssert;

/**
 * Test for {@link PmAttrLocalDateTimeImpl}.
 *
 * @author HLLY
 */
public class PmAttrLocalDateTimeImplTest {

  private TestPm testPm = new TestPm();

  @Before
  public void setUp() {
    testPm.setPmLocale(Locale.ENGLISH);
    testPm.setPmTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void conversionError() {
    testPm.dateTime.setValueAsString("320/04/2012 10:00");
    assertFalse(testPm.dateTime.isPmValid());
    assertSingleErrorMessage(testPm.dateTime,
        "The value of the field \"local DateTime\" cannot be interpreted. Please use the format \"dd/MM/yyyy HH:mm\".");
  }

  @Test
  public void parseSuccess() {
    PmAssert.setValueAsString(testPm.dateTime, "29/05/2012 10:00");
  }

  @Test
  public void testBeanValueConversion() {
    LocalDateTime localDate = new LocalDateTime(2012, 6, 6, 10, 0);
    PmAssert.setValue(testPm.dateTime, localDate);
    assertTrue(testPm.dateTime.isPmValid());
    assertEquals("06/06/2012 10:00", testPm.dateTime.getValueAsString());
    assertEquals(localDate, testPm.dateTime.getBackingValue());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testLocalDateComparison() {
    PmAttrLocalDateTimeImpl datePm1 = new PmAttrLocalDateTimeImpl(testPm);
    datePm1.setValue(new LocalDateTime(2000, 1, 1, 10, 0));
    PmAttrLocalDateTimeImpl datePm2 = new PmAttrLocalDateTimeImpl(testPm);
    datePm2.setValue(new LocalDateTime(2000, 1, 1, 11, 0));
    PmAttrLocalDateTimeImpl datePm3 = new PmAttrLocalDateTimeImpl(testPm);
    datePm3.setValue(new LocalDateTime(2001, 1, 1, 10, 0));
    PmAttrLocalDateTimeImpl datePm4 = new PmAttrLocalDateTimeImpl(testPm);
    datePm4.setValue(new LocalDateTime(2000, 1, 1, 10, 0));

    // x < y
    assertEquals(datePm1.compareTo(datePm2), -1);
    assertEquals(datePm2.compareTo(datePm3), -1);
    assertEquals(datePm1.compareTo(datePm3), -1);

    // x > y
    assertEquals(datePm2.compareTo(datePm1), 1);
    assertEquals(datePm3.compareTo(datePm2), 1);
    assertEquals(datePm3.compareTo(datePm1), 1);

    // x = y
    assertEquals(datePm1.compareTo(datePm1), 0);
    assertEquals(datePm1.compareTo(datePm4), 0);
  }

  @Test
  public void testWithTimeZone() {
    PmAttrLocalDateTimeImpl d = testPm.dateTimeWithTzConverter;
    d.setBackingValue(new LocalDateTime(2013, 11, 01, 18, 00));
    assertEquals("01/11/2013 18:00", d.getValueAsString());

    testPm.setPmTimeZone(TimeZone.getTimeZone("Etc/GMT-1"));
    assertEquals("01/11/2013 19:00", d.getValueAsString());

    setValueAsString(d, "01/11/2013 21:00");
    assertEquals("01/11/2013 20:00", d.getBackingValue().toString("dd/MM/yyyy HH:mm"));

    testPm.setPmTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
    d.setValueAsString("01/11/2013 21:00");
    assertEquals("01/11/2013 13:00", d.getBackingValue().toString("dd/MM/yyyy HH:mm"));
    assertEquals("01/11/2013 21:00", d.getValueAsString());
    assertEquals("01/11/2013 21:00", d.getValueLocalized());

    testPm.setPmTimeZone(TimeZone.getTimeZone("Etc/GMT-1"));
    assertEquals("01/11/2013 13:00", d.getBackingValue().toString("dd/MM/yyyy HH:mm"));
    assertEquals("01/11/2013 14:00", d.getValueAsString());
  }

  static class TestPm extends PmConversationImpl {

    @PmTitleCfg(title="local DateTime")
    public PmAttrLocalDateTimeImpl dateTime = new PmAttrLocalDateTimeImpl(this);

    @PmTitleCfg(title="local DateTime with time zone")
    @PmAttrCfg(valueConverter = LocalDateTimeTimeZoneConverter.class)
    public PmAttrLocalDateTimeImpl dateTimeWithTzConverter = new PmAttrLocalDateTimeImpl(this);
  }
}
