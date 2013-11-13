package org.pm4j.core.joda.impl;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.joda.PmAttrLocalDateTime;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmWithTimeZone;
import org.pm4j.core.pm.impl.converter.ValueConverterWithTimeZoneBase;

/**
 * Pm Attribute for a {@link LocalDateTime}.
 *
 * This field can handle multiple input formats to be defined ;-separated in the
 * resources with key suffix "_format" appended to the fields resource key.
 *
 * @author olaf boede
 */
public class PmAttrLocalDateTimeImpl
  extends PmAttrBase<LocalDateTime, LocalDateTime>
  implements PmAttrLocalDateTime, PmWithTimeZone {

  /**
   * @param pmParent
   *          The PM parent.
   */
  public PmAttrLocalDateTimeImpl(PmObject pmParent) {
    super(pmParent);
  }

  /** The default implementation provides the result of {@link PmConversation#getPmTimeZone()}. */
  @Override
  public TimeZone getPmTimeZone() {
    return getPmConversation().getPmTimeZone();
  }

  @Override
  protected String getFormatDefaultResKey() {
    return FORMAT_DEFAULT_RES_KEY;
  }

  /** @deprecated Compare operations base on PMs are no longer supported. That can be done on bean level. */
  @Deprecated
  @Override
  public int compareTo(PmObject otherPm) {
    return CompareUtil.compare(getValue(), ((PmAttrLocalDateTime) otherPm).getValue());
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    /** Sets the default max length is the length of the date format pattern. */
    MetaData md = new MetaData(11);
    // Configure the default converters. Is done before <code>initMetaData</code> to allow
    // annotation based customization.
    md.setStringConverter(new LocalDateTimeStringConverter());
    return md;
  }

  /**
   * Converts the external value representation to a PM time zone related value.
   */
  public static class ValueConverterWithTimeZone extends ValueConverterWithTimeZoneBase<LocalDateTime, LocalDateTime> {
    @Override
    public LocalDateTime toExternalValue(PmAttr<LocalDateTime> pmAttr, LocalDateTime i) {
      DateTime utcDt = i.toDateTime(getBackingValueDateTimeZone());
      DateTime tzDt = utcDt.toDateTime(DateTimeZone.forTimeZone(getPmTimeZone(pmAttr)));
      return tzDt.toLocalDateTime();
    }

    @Override
    public LocalDateTime toInternalValue(PmAttr<LocalDateTime> pmAttr, LocalDateTime e) {
      DateTime tzDt = e.toDateTime(DateTimeZone.forTimeZone(getPmTimeZone(pmAttr)));
      DateTime utcDt = tzDt.toDateTime(getBackingValueDateTimeZone());
      return utcDt.toLocalDateTime();
    }

    /** The default implementation provides {@link DateTimeZone#UTC}. */
    protected DateTimeZone getBackingValueDateTimeZone() {
      return DateTimeZone.UTC;
    }

  }

  /**
   * Multi format string converter for Joda {@link LocalDateTime}.
   */
  public static class LocalDateTimeStringConverter extends JodaStringConverterBase<LocalDateTime> {
    @Override
    protected LocalDateTime parseJodaType(DateTimeFormatter fmt, String stringValue) {
      return fmt.parseLocalDateTime(stringValue);
    }

    @Override
    protected String printJodaType(DateTimeFormatter fmt, LocalDateTime value) {
      return fmt.print(value);
    }
  }
}
