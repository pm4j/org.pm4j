package org.pm4j.core.joda.impl;

import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

public class LocalDateConverterTest {

  private static final String ddMMMyyyy = "dd-MMM-yyyy";

  @Test
  public void testConversionJan() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("01-Jan-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "01-Jan-2013");
  }

  @Test
  public void testConversionJAN() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("01-JAN-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "01-Jan-2013");
  }

  @Test
  public void testConversionFeb() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("05-Feb-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "05-Feb-2013");
  }

  @Test
  public void testConversionFEB() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("05-FEB-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "05-Feb-2013");
  }

  @Test
  public void testConversionMar() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("12-Mar-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "12-Mar-2013");
  }

  @Test
  public void testConversionMAR() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("12-MAR-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "12-Mar-2013");
  }

  @Test
  public void testConversionApr() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("13-Apr-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "13-Apr-2013");
  }

  @Test
  public void testConversionAPR() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("13-APR-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "13-Apr-2013");
  }

  @Test
  public void testConversionMay() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("15-May-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "15-May-2013");
  }

  @Test
  public void testConversionMAY() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("15-MAY-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "15-May-2013");
  }

  @Test
  public void testConversionJun() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("17-Jun-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "17-Jun-2013");
  }

  @Test
  public void testConversionJUN() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("17-JUN-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "17-Jun-2013");
  }

  @Test
  public void testConversionJul() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("20-Jul-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "20-Jul-2013");
  }

  @Test
  public void testConversionJUL() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("20-JUL-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "20-Jul-2013");
  }

  @Test
  public void testConversionAug() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("23-Aug-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "23-Aug-2013");
  }

  @Test
  public void testConversionAUG() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("23-AUG-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "23-Aug-2013");
  }

  @Test
  public void testConversionSep() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("25-Sep-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "25-Sep-2013");
  }

  @Test
  public void testConversionSEP() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("25-SEP-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "25-Sep-2013");
  }

  @Test
  public void testConversionOct() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("25-Oct-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "25-Oct-2013");
  }

  @Test
  public void testConversionOCT() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("25-OCT-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "25-Oct-2013");
  }

  @Test
  public void testConversionNov() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("28-Nov-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "28-Nov-2013");
  }

  @Test
  public void testConversionNOV() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("28-NOV-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "28-Nov-2013");
  }

  @Test
  public void testConversionDec() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("30-Dec-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "30-Dec-2013");
  }

  @Test
  public void testConversionDEC() {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ddMMMyyyy).withLocale(Locale.ENGLISH);
    LocalDate localDate = fmt.parseLocalDate("30-DEC-2013");
    Assert.assertEquals(localDate.toString(ddMMMyyyy, Locale.ENGLISH), "30-Dec-2013");
  }

}
