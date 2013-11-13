package org.pm4j.core.joda.impl;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.joda.PmAttrLocalTime;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmWithTimeZone;
import org.pm4j.core.pm.impl.converter.ValueConverterWithTimeZoneBase;

/**
 * PM attribute for a {@link LocalTime}.
 *
 * This field can handle multiple input formats to be defined ;-separated in the
 * resources with key suffix "_format" appended to the fields resource key.
 *
 * @author Olaf Kossak
 * @since 0.6.12
 */
public class PmAttrLocalTimeImpl2
    extends PmAttrBase<LocalTime, LocalTime>
    implements PmAttrLocalTime, PmWithTimeZone {

  /**
   * @param pmParent
   *          The PM parent.
   */
  public PmAttrLocalTimeImpl2(PmObject pmParent) {
    super(pmParent);
  }

  /** The default implementation provides the result of {@link PmConversation#getPmTimeZone()}. */
  @Override
  public TimeZone getPmTimeZone() {
    return getPmConversation().getPmTimeZone();
  }

  /** Uses {@link PmAttrLocalTime#FORMAT_DEFAULT_RES_KEY}. */
  @Override
  protected String getFormatDefaultResKey() {
    return PmAttrLocalTime.FORMAT_DEFAULT_RES_KEY;
  }

  /** @deprecated Compare operations base on PMs are no longer supported. That can be done on bean level. */
  @Deprecated
  @Override
  public int compareTo(PmObject otherPm) {
    return CompareUtil.compare(getValue(), ((PmAttrLocalTime) otherPm).getValue());
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    /** Sets the default max length is the length of the time format pattern. */
    // TODO oboede: needs to be derived from the format.
    MetaData md = new MetaData(8);
    // Configure the default converters. Is done before <code>initMetaData</code> to allow
    // annotation based customization.
    md.setStringConverter(new LocalTimeStringConverter());
    return md;
  }

  /**
   * Converts the external value representation to a PM time zone related value.
   */
  public static class ValueConverterWithTimeZone extends ValueConverterWithTimeZoneBase<LocalTime, LocalTime> {
    @Override
    public LocalTime toExternalValue(PmAttr<LocalTime> pmAttr, LocalTime i) {
      DateTime utcDt = i.toDateTimeToday(getBackingValueDateTimeZone());
      DateTime tzDt = utcDt.toDateTime(DateTimeZone.forTimeZone(getPmTimeZone(pmAttr)));
      return tzDt.toLocalTime();
    }

    @Override
    public LocalTime toInternalValue(PmAttr<LocalTime> pmAttr, LocalTime e) {
      DateTime tzDt = e.toDateTimeToday(DateTimeZone.forTimeZone(getPmTimeZone(pmAttr)));
      DateTime utcDt = tzDt.toDateTime(getBackingValueDateTimeZone());
      return utcDt.toLocalTime();
    }

    /** The default implementation provides {@link DateTimeZone#UTC}. */
    protected DateTimeZone getBackingValueDateTimeZone() {
      return DateTimeZone.UTC;
    }

  }

  /**
   * Multi format string converter for Joda {@link LocalTime}.
   */
  public static class LocalTimeStringConverter extends JodaStringConverterBase<LocalTime> {
    @Override
    protected LocalTime parseJodaType(DateTimeFormatter fmt, String stringValue) {
      return fmt.parseLocalTime(stringValue);
    }

    @Override
    protected String printJodaType(DateTimeFormatter fmt, LocalTime value) {
      return fmt.print(value);
    }
  }
}
