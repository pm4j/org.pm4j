package org.pm4j.common.converter.string;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * String converter helper methods.
 *
 * @author Olaf Boede
 */
public class StringConverterUtil {

  /** The default separator string used in case of a multi-format resource string specification. */
  private static String formatSplitString = "|";

  public static void setFormatSplitString(String s) {
    assert s != null && s.length() > 0;
    formatSplitString = s;
  }

  /**
   * Default implementation, may be overridden by implementations:
   * The last format definition returned by {@link #getParseFormats(PmAttr)}.
   * <p>
   * Is used for the method {@link PmAttr#getValueAsString()}.
   * <p>
   * It is also intended to be used by UI help constructs such as calendar
   * popups which provide their data as strings.
   *
   * @return The last item of the result of {@link #getParseFormats(PmAttr)}
   */
  public static String getOutputFormat(StringConverterCtxt ctxt) {
    String[] formats = getParseFormats(ctxt);
    return formats[formats.length-1];
  }

  /**
   * Builds an array of all format strings for the <code>pmAttr</code>.
   * @param pmAttr The pmAttr.
   * @return Format strings.
   */
  public static String[] getParseFormats(StringConverterCtxt ctxt) {
    String allFormatsString = StringUtils.defaultString(ctxt.getConverterCtxtFormatString());
    String[] formats = StringUtils.split(allFormatsString, formatSplitString);
    return formats;
  }

  public static <T> String convertToString(StringConverter<T> converter, T value) {
    return converter.valueToString(new StringConverterCtxtImpl(null, Locale.getDefault(), TimeZone.getDefault()), value);
  }

  public static <T> String convertToString(StringConverter<T> converter, T value, String format) {
    return converter.valueToString(new StringConverterCtxtImpl(format, Locale.getDefault(), TimeZone.getDefault()), value);
  }

  public static <T> String convertToString(StringConverter<T> converter, T value, String format, Locale locale) {
    return converter.valueToString(new StringConverterCtxtImpl(format, locale, TimeZone.getDefault()), value);
  }

  public static <T> String convertToString(StringConverter<T> converter, T value, String format, Locale locale, TimeZone timeZone) {
    return converter.valueToString(new StringConverterCtxtImpl(format, locale, timeZone), value);
  }

  public static <T> T convertToValue(StringConverter<T> converter, String string) {
    return convertToValue(converter, string, null, Locale.getDefault(), TimeZone.getDefault());
  }

  public static <T> T convertToValue(StringConverter<T> converter, String string, String format) {
    return convertToValue(converter, string, format, Locale.getDefault(), TimeZone.getDefault());
  }

  public static <T> T convertToValue(StringConverter<T> converter, String string, String format, Locale locale) {
    return convertToValue(converter, string, format, locale, TimeZone.getDefault());
  }

  public static <T> T convertToValue(StringConverter<T> converter, String string, String format, Locale locale, TimeZone timeZone) {
    try {
      return converter.stringToValue(new StringConverterCtxtImpl(format, locale, timeZone), string);
    } catch (StringConverterParseException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

}
