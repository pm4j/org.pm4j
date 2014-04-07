package org.pm4j.common.converter.joda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.pm4j.common.converter.string.StringConverterUtil;
import org.pm4j.common.converter.string.joda.LocalDateStringConverter;
import org.pm4j.common.exception.CheckedExceptionWrapper;

public class LocalDateStringConverterTest {

  private final LocalDateStringConverter converter = new LocalDateStringConverter();
  private final LocalDate testDate = new LocalDate(2014, 4, 7);

  @Test
  public void testSingleFormatToString() {
    assertEquals("2014/04/07", StringConverterUtil.convertToString(converter, testDate, "yyyy/MM/dd"));
  }

  @Test
  public void testLocalizedFormatToString() {
    assertEquals("2014/Apr/07", StringConverterUtil.convertToString(converter, testDate, "yyyy/MMM/dd", Locale.ENGLISH));
    assertEquals("2014/avr./07", StringConverterUtil.convertToString(converter, testDate, "yyyy/MMM/dd", Locale.FRENCH));
  }

  @Test
  public void testSingleFormatToValue() {
    assertEquals(testDate, StringConverterUtil.convertToValue(converter, "2014/04/07", "yyyy/MM/dd"));
  }

  @Test
  public void testMultiFormatToString() {
    assertEquals("2014-04-07", StringConverterUtil.convertToString(converter, testDate, "yyyy/MM/dd|yyyy-MM-dd"));
  }

  @Test
  public void testMultiFormatToValue() {
    assertEquals(testDate, StringConverterUtil.convertToValue(converter, "2014/04/07", "yyyy/MM/dd|yyyy-MM-dd"));
    assertEquals(testDate, StringConverterUtil.convertToValue(converter, "2014-04-07", "yyyy/MM/dd|yyyy-MM-dd"));
  }

  @Test
  public void testFormatFailure() {
    try {
      StringConverterUtil.convertToValue(converter, "2014-04-07", "yyyy/MM/dd");
      fail("invalid format should fail");
    } catch (CheckedExceptionWrapper e) {
      assertTrue(e.getMessage().startsWith("Unable to parse '2014-04-07'. Supported formats: [yyyy/MM/dd]"));
      assertEquals("Invalid format: \"2014-04-07\" is malformed at \"-04-07\"", e.getCause().getCause().getMessage());
    }
  }

}
