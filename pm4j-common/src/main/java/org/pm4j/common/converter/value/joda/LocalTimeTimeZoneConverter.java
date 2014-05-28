package org.pm4j.common.converter.value.joda;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.pm4j.common.converter.value.ValueConverterCtxt;

/**
 * Converts the external value representation to a PM time zone related value.
 *
 * @author Olaf Boede
 */
public class LocalTimeTimeZoneConverter extends JodaTimeZoneConverterBase<LocalTime, LocalTime> {

  @Override
  public LocalTime toExternalValue(ValueConverterCtxt ctxt, LocalTime i) {
    if (i == null) {
      return null;
    }
    DateTime internalDt = i.toDateTimeToday(getInternalValueDateTimeZone(ctxt));
    DateTime externalDt = internalDt.toDateTime(getExternalValueDateTimeZone(ctxt));
    return externalDt.toLocalTime();
  }

  @Override
  public LocalTime toInternalValue(ValueConverterCtxt ctxt, LocalTime e) {
    if (e == null) {
      return null;
    }
    DateTime externalDt = e.toDateTimeToday(getExternalValueDateTimeZone(ctxt));
    DateTime internalDt = externalDt.toDateTime(getInternalValueDateTimeZone(ctxt));
    return internalDt.toLocalTime();
  }

}
