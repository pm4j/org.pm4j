package org.pm4j.common.converter.joda;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.pm4j.common.converter.value.ValueConverterCtxt;
import org.pm4j.common.converter.value.ValueConverterCtxtImpl;
import org.pm4j.common.converter.value.joda.LocalDateTimeTimeZoneConverter;
import org.pm4j.common.converter.value.joda.LocalTimeTimeZoneConverter;

/**
 * A test for {@link LocalTimeTimeZoneConverter}.
 *
 * @author Olaf Boede
 */
public class LocalDateTimeTimeZoneConverterTest {

  private LocalDateTimeTimeZoneConverter converter = new LocalDateTimeTimeZoneConverter();

  @Test
  public void convertBetweenInternalUtcAndExternalSingapore() {
    ValueConverterCtxt ctxt = new ValueConverterCtxtImpl(TimeZone.getTimeZone("Etc/GMT-8"));

    assertEquals(new LocalDateTime(2014, 9, 23, 19, 00), converter.toExternalValue(ctxt, new LocalDateTime(2014, 9, 23, 11, 00)));
    assertEquals(new LocalDateTime(2014, 9, 23, 11, 00), converter.toInternalValue(ctxt, new LocalDateTime(2014, 9, 23, 19, 00)));
  }

  @Test
  public void convertBetweenExternalSingaporeAndInternalAddis() {
    LocalDateTimeTimeZoneConverter converter = new LocalDateTimeTimeZoneConverter() {
      @Override
      protected DateTimeZone getInternalValueDateTimeZone(ValueConverterCtxt ctxt) {
        return DateTimeZone.forID("Africa/Addis_Ababa");
      }
    };
    ValueConverterCtxt ctxt = new ValueConverterCtxtImpl(TimeZone.getTimeZone("Etc/GMT-8"));

    assertEquals(new LocalDateTime(2014, 9, 23, 16, 00), converter.toExternalValue(ctxt, new LocalDateTime(2014, 9, 23, 11, 00)));
    assertEquals(new LocalDateTime(2014, 9, 23, 11, 00), converter.toInternalValue(ctxt, new LocalDateTime(2014, 9, 23, 16, 00)));
  }

}
