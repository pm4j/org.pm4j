package org.pm4j.core.pm.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.converter.MultiFormatConverter;
import org.pm4j.core.pm.impl.converter.MultiFormatParserBase;
import org.pm4j.core.pm.impl.converter.PmConverterBase;

/**
 * Implements a PM attribute for {@link Date} values.
 *
 * @author olaf boede
 */
public class PmAttrDateImpl extends PmAttrBase<Date, Date> implements PmAttrDate {

  public PmAttrDateImpl(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * The default format key {@link #RESKEY_DEFAULT_FORMAT_PATTERN} applies when no
   * special format is defined (either by resource key with postfix or
   * annotation).
   *
   * @see PmAttrBase#getFormatString()
   */
  @Override
  protected String getFormatDefaultResKey() {
    return RESKEY_DEFAULT_FORMAT_PATTERN;
  }

  @Override
  public String getOutputFormat() {
    return PmLocalizeApi.getOutputFormatString(this);
  }

  /** @deprecated Compare operations on PMs are no longer supported. This is done on bean level now. */
  @Deprecated
  @Override
  public int compareTo(PmObject otherPm) {
    return PmUtil.getAbsoluteName(this).equals(PmUtil.getAbsoluteName(otherPm))
              ? CompareUtil.compare(getValue(), ((PmAttrDate)otherPm).getValue())
              : super.compareTo(otherPm);
  }

  @Override
  protected Converter<Date> getStringConverterImpl() {
    return DateStringConverter.INSTANCE;
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    // The default max length is the length of the date format pattern.
    return new MetaData(20);
  }


  /** A date string converter with multi format support. */
  public static class DateStringConverter extends PmConverterBase<Date> implements MultiFormatConverter {

    public static final DateStringConverter INSTANCE = new DateStringConverter();

    /**
     * Implementation of converter capable of handling multiple input formats.
     */
    private MultiFormatParserBase<Date> multiFormatParser = new MultiFormatParserBase<Date>() {

      @Override
      protected Date parseValue(String s, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        sdf.setTimeZone(getTimeZone(pmAttr));

        // we will not allow dates of the form 00.00.08 etc, hence lenient is set to false
        sdf.setLenient(false);

        return sdf.parse(s);
      }

    };

    @Override
    public Date stringToValue(PmAttr<?> pmAttr, String s) {
      return multiFormatParser.parseString(pmAttr, s);
    }

    @Override
    public String valueToString(PmAttr<?> pmAttr, Date value) {
      String outputFormat = multiFormatParser.getOutputFormat(pmAttr);
      try {
        TimeZone timeZone = getTimeZone(pmAttr);
        return FastDateFormat.getInstance(outputFormat, timeZone, pmAttr.getPmConversation().getPmLocale()).format(value);
      }
      catch (RuntimeException e) {
        throw new PmRuntimeException(pmAttr, "Unable to apply format '" +
                                           outputFormat + "' to value '" + value + "'.");
      }
    }

    @Override
    public String getOutputFormat(PmAttr<?> pmAttr) {
      return multiFormatParser.getOutputFormat(pmAttr);
    }

    private TimeZone getTimeZone(PmAttr<?> pmAttr) {
      TimeZone timeZone = (pmAttr instanceof PmWithTimeZone)
          ? ((PmWithTimeZone) pmAttr).getPmTimeZone()
          : pmAttr.getPmConversation().getPmTimeZone();
      return timeZone;
    }

  }

  /**
   * A subclass with a tooltip that provides a hint about the format.
   */
  // XXX olaf: should this stay in the core?
  public static class WithFormatTooltip<T_BACKING_DATE> extends PmAttrDateImpl {

    public static final String RESKEY_DATEATTR_FORMAT_TOOLTIP = "pmAttrDate.WithFormatTooltip_tooltip";

    public WithFormatTooltip(PmElementBase pmParentBean) {
      super(pmParentBean);
    }

    @Override
    protected String getPmTooltipImpl() {
      String fmt = getOutputFormat();
      return PmLocalizeApi.localize(this, RESKEY_DATEATTR_FORMAT_TOOLTIP,
                      fmt, FastDateFormat.getInstance(fmt).format(new Date()));
    }
  }

}
