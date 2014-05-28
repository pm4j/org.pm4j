package org.pm4j.common.converter.string;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

/** A date string converter with multi format support. */
public class StringConverterDate extends StringConverterBase<Date, StringConverterCtxt> {

  public static final StringConverterDate INSTANCE = new StringConverterDate();

  /**
   * Implementation of converter capable of handling multiple input formats.
   */
  private MultiFormatParserBase<Date> multiFormatParser = new MultiFormatParserBase<Date>() {

    @Override
    protected Date parseValue(StringConverterCtxt ctxt, String s, String format) throws ParseException {
      if (s == null) {
        return null;
      }
      SimpleDateFormat sdf = new SimpleDateFormat(format, ctxt.getConverterCtxtLocale());
      sdf.setTimeZone(ctxt.getConverterCtxtTimeZone());

      // We currently not support partial dates.
      // The multi-format feature is currently sufficient.
      // But in future that may be configurable.
      sdf.setLenient(false);
      return sdf.parse(s);
    }
  };

  @Override
  protected Date stringToValueImpl(StringConverterCtxt ctxt, String s) throws ParseException {
    return (s != null && !s.isEmpty())
        ? multiFormatParser.parseString(ctxt, s)
        : null;
  }

  @Override
  protected String valueToStringImpl(StringConverterCtxt ctxt, Date value) {
    if (value == null) {
      return null;
    }
    String outputFormat = StringConverterUtil.getOutputFormat(ctxt);
    TimeZone timeZone = ctxt.getConverterCtxtTimeZone();
    Locale locale = ctxt.getConverterCtxtLocale();
    return FastDateFormat.getInstance(outputFormat, timeZone, locale).format(value);
  }
}