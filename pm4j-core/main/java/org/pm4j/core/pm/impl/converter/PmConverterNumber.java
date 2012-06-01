package org.pm4j.core.pm.impl.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmAttr;

/**
 * Base class for number type converters.
 *
 * @author olaf boede
 *
 * @param <T> The concrete {@link Number} type to convert.
 */
public abstract class PmConverterNumber<T extends Number> extends PmConverterSerializeableBase<T>{

  /**
   * Parser for all number types. Adjustments of the implementing classes are done in the implementations of
   * the abstract methods.
   */
  private MultiFormatParserBase<T> multiFormatParser = new MultiFormatParserBase<T>() {

    @Override
    protected String getDefaultFormatPattern() {
      return getDefaultNumberFormatPattern();
    }

    @Override
    protected T parseValue(String s, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException {
      NumberFormat f = getNumberFormat(format, locale);
        return convertParseResultToType(f.parse(s));
    }

  };

  /**
   * This method converts the parsed number to the correct type.
   *
   * @param parsed parsed number
   * @return correct Type
   */
  protected abstract T convertParseResultToType(Number parsed);

  /**
   * Define the specific number format in this method.
   * @return Format string.
   */
  protected abstract String getDefaultNumberFormatPattern();

  public T stringToValue(PmAttr<?> pmAttr, String s) {
    return multiFormatParser.parseString(pmAttr, s);
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, T v) {
    NumberFormat f = getNumberFormat(multiFormatParser.getOutputFormat(pmAttr), pmAttr != null ? pmAttr.getPmConversation().getPmLocale() : Locale.getDefault());
    return f.format(v);
  }

  /**
   * Gets the number format for the given String and Locale.
   *
   * @param format
   *          The format string.
   * @param locale
   *          The locale to be used.
   * @return The associated number format.<br>
   *         In case of an empty or <code>null</code> result of
   *         {@link PmAttr#getFormatString()}, a {@link DecimalFormat} for the
   *         current locale of the given pmAttr will be returned.
   */
  protected NumberFormat getNumberFormat(String format, Locale locale) {
    return (StringUtils.isBlank(format))
        ? NumberFormat.getNumberInstance(locale)
        : new DecimalFormat(format, new DecimalFormatSymbols(locale));
  }


}
