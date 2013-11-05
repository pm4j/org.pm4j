package org.pm4j.core.joda.impl;

import java.util.TimeZone;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.joda.PmAttrLocalDate;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmWithTimeZone;

/**
 * PM attribute for a {@link LocalDate}.
 *
 * This field can handle multiple input formats to be defined ;-separated in the
 * resources with key suffix "_format" appended to the fields resource key.
 *
 * @author Harm Gnoyke
 * @author Olaf Boede
 */
public class PmAttrLocalDateImpl
  extends PmAttrBase<LocalDate, LocalDate>
  implements PmAttrLocalDate, PmWithTimeZone {

  /**
   * @param pmParent
   *          The PM parent.
   */
  public PmAttrLocalDateImpl(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * The default implementation provides the result of
   * {@link PmConversation#getPmTimeZone()}.
   */
  @Override
  public TimeZone getPmTimeZone() {
    return getPmConversation().getPmTimeZone();
  }

  @Override
  protected Converter<LocalDate> getConverter() {
    return LocalDateStringConverter.INSTANCE;
  }

  /** Provides {@link PmAttrLocalDate.FORMAT_DEFAULT_RES_KEY}. */
  @Override
  protected String getFormatDefaultResKey() {
    return PmAttrLocalDate.FORMAT_DEFAULT_RES_KEY;
  }

  /** @deprecated Compare operations base on PMs are no longer supported. That can be done on bean level. */
  @Deprecated
  @Override
  public int compareTo(PmObject otherPm) {
    return CompareUtil.compare(getValue(), ((PmAttrLocalDate) otherPm).getValue());
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    /** Sets the default max length is the length of the date format pattern. */
    // TODO oboede: needs to be derived from the format.
    return new MetaData(11);
  }

  /**
   * Multi format string converter for Joda {@link LocalDate}.
   */
  public static class LocalDateStringConverter extends JodaStringConverterBase<LocalDate> {

    /** A shared default instance that may be used like a singleton. */
    public static final LocalDateStringConverter INSTANCE = new LocalDateStringConverter();

    @Override
    protected LocalDate parseJodaType(DateTimeFormatter fmt, String stringValue) {
      return fmt.parseLocalDate(stringValue);
    }

    @Override
    protected String printJodaType(DateTimeFormatter fmt, LocalDate value) {
      return fmt.print(value);
    }

  }
}
