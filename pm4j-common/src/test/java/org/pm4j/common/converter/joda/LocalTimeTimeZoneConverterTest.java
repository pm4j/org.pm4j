package org.pm4j.common.converter.joda;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.joda.time.LocalTime;
import org.junit.Test;
import org.pm4j.common.converter.value.ValueConverterCtxt;
import org.pm4j.common.converter.value.ValueConverterCtxtImpl;
import org.pm4j.common.converter.value.joda.LocalTimeTimeZoneConverter;

/**
 * A test for {@link LocalTimeTimeZoneConverter}.
 *
 * @author Olaf Boede
 */
public class LocalTimeTimeZoneConverterTest {

  private LocalTimeTimeZoneConverter converter = new LocalTimeTimeZoneConverter();

  @Test
  public void convertBetweenInternalUtcAndExternalSingapore() {
    ValueConverterCtxt ctxt = new ValueConverterCtxtImpl(TimeZone.getTimeZone("Etc/GMT-8"));

    assertEquals(new LocalTime(19, 00), converter.toExternalValue(ctxt, new LocalTime(11, 00)));
    assertEquals(new LocalTime(11, 00), converter.toInternalValue(ctxt, new LocalTime(19, 00)));
  }

  @Test
  public void convertUtcAndBack() {
    ValueConverterCtxt ctxt = new ValueConverterCtxtImpl(TimeZone.getTimeZone("UTC"));

    assertEquals(new LocalTime(11, 00), converter.toExternalValue(ctxt, new LocalTime(11, 00)));
    assertEquals(new LocalTime(11, 00), converter.toInternalValue(ctxt, new LocalTime(11, 00)));
  }

}
