package org.pm4j.common.converter.value.joda;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.pm4j.common.converter.value.ValueConverterCtxt;

/**
 * Converts the external value representation to a PM time zone related value.
 *
 * @author Olaf Boede
 */
public class LocalDateTimeTimeZoneConverter extends JodaTimeZoneConverterBase<LocalDateTime, LocalDateTime> {
    @Override
    public LocalDateTime toExternalValue(ValueConverterCtxt ctxt, LocalDateTime i) {
      if (i == null) {
        return null;
      }
      DateTime internalDt = i.toDateTime(getInternalValueDateTimeZone(ctxt));
      DateTime externalDt = internalDt.toDateTime(getExternalValueDateTimeZone(ctxt));
      return externalDt.toLocalDateTime();
    }

    @Override
    public LocalDateTime toInternalValue(ValueConverterCtxt ctxt, LocalDateTime e) {
      if (e == null) {
        return null;
      }
      DateTime externalDt = e.toDateTime(getExternalValueDateTimeZone(ctxt));
      DateTime internalDt = externalDt.toDateTime(getInternalValueDateTimeZone(ctxt));
      return internalDt.toLocalDateTime();
    }

}
