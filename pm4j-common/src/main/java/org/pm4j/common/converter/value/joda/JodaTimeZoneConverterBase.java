package org.pm4j.common.converter.value.joda;

import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.pm4j.common.converter.value.ValueConverter;
import org.pm4j.common.converter.value.ValueConverterCtxt;

/**
 * Convenience base class for {@link PmAttr#ValueConverter}'s that need to consider time zone information.
 *
 * @param <E> The external attribute value type.
 * @param <I> The internal attribute value type.
 *
 * @author Olaf Boede
 */
public abstract class JodaTimeZoneConverterBase<E, I> implements ValueConverter<E, I> {

  /**
   * Retrieves the {@link TimeZone} from the given context.
   *
   * @param ctxt The converter context. Provides time zone information.
   * @return The {@link DateTimeZone}. Never <code>null</code>.
   */
  protected DateTimeZone getExternalValueDateTimeZone(ValueConverterCtxt ctxt) {
    TimeZone tz = ctxt.getConverterCtxtTimeZone();
    if (tz == null) {
      throw new RuntimeException("Time zone information should not be null. Context: " + ctxt);
    }
    return DateTimeZone.forTimeZone(tz);
  }

  /** The default implementation provides {@link DateTimeZone#UTC}. */
  protected DateTimeZone getInternalValueDateTimeZone(ValueConverterCtxt ctxt) {
    return DateTimeZone.UTC;
  }
}
