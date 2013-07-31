package org.pm4j.core.joda.impl;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.pm4j.core.joda.PmAttrLocalDateTime;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * An attribute that presents a time zone based date and time. The time zone defaults to the user's
 * time zone configured in {@link PmConversation#getPmTimeZone()}.
 *
 * The user can then enter date and time information in the respective time zone. The backing
 * value of this class will hold a {@link DateTime} with the UTC representation.
 *
 * @author Harm Gnoyke, olaf boede
 */
public class PmAttrLocalDateTimeOnDateTimeImpl extends PmAttrBase<LocalDateTime, DateTime> implements
    PmAttrLocalDateTime {

  public PmAttrLocalDateTimeOnDateTimeImpl(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * Provides the time zone that corresponds to the time zone configured in the
   * {@link PmConversation}.
   *
   * Uses {@link DateTimeZone#getDefault()} if none is configured.
   *
   * @return the time zone.
   */
  public DateTimeZone getDateTimeZone() {
    TimeZone tz = getPmConversation().getPmTimeZone();
    return (tz != null)
        ? DateTimeZone.forTimeZone(tz)
        : DateTimeZone.getDefault();
  }

  @Override
  public LocalDateTime convertBackingValueToPmValue(DateTime backingValue) {
    return backingValue.withZone(getDateTimeZone()).toLocalDateTime();
  }

  @Override
  public DateTime convertPmValueToBackingValue(LocalDateTime pmAttrValue) {
    return pmAttrValue.toDateTime(getDateTimeZone());
  }

  /**
   * The default format key {@link #RESKEY_DEFAULT_FORMAT_PATTERN} applies when
   * no special format is defined (either by resource key with suffix or
   * annotation).
   *
   * @see PmAttrBase#getFormatString()
   */
  @Override
  protected String getFormatDefaultResKey() {
    return PmAttrLocalDateTime.FORMAT_DEFAULT_RES_KEY;
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    // TODO oboede: define a pattern based default length.
    return new MetaData(11);
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    ((MetaData) metaData).setConverterDefault(PmConverterLocalDateTime.INSTANCE);
  }
}
