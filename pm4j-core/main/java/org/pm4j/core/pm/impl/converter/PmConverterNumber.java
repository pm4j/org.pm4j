 package org.pm4j.core.pm.impl.converter;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmResourceRuntimeException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;

 /**
  * Base class for number type converters.
 *
 * @author olaf boede
 *
 * @param <T> The concrete {@link Number} type to convert.
 */
public class PmConverterNumber<T extends Number> extends PmConverterSerializeableBase<T>{

  private final Constructor<T> numberCtor;
  private String defaultPattern = "#0";

  public PmConverterNumber(Class<T> numberClass) {
        try {
          numberCtor = numberClass.getConstructor(String.class);
        } catch (Exception e) {
          throw new PmRuntimeException("Number class without string constructor is not supported. Class: " + numberClass);
    }


  }

  @Override
  public T stringToValue(PmAttr<?> pmAttr, String s) {
      try {
        return numberCtor.newInstance(s.trim());
      } catch (Exception e) {
        throw new PmResourceRuntimeException(pmAttr, PmConstants.MSGKEY_VALIDATION_NUMBER_CONVERSION_FROM_STRING_FAILED, s);
      }
  }

  T stringToValue2(PmAttr<?> pmAttr, String s) {
    try {
      T value = multiFormatParser.parseString(pmAttr, s);
      if (value != null && value.getClass() != numberCtor.getDeclaringClass()) {
        return numberCtor.newInstance(value.toString());
      }
      else {
        return value;
      }

    } catch (Exception e) {
      throw new PmResourceRuntimeException(pmAttr, PmConstants.MSGKEY_VALIDATION_NUMBER_CONVERSION_FROM_STRING_FAILED, s);
    }
  }

  String valueToString2(org.pm4j.core.pm.PmAttr<?> pmAttr, T v) {
    String outputFormatString = multiFormatParser.getOutputFormat(pmAttr);
    NumberFormat format = getNumberFormat(pmAttr.getPmConversation().getPmLocale(), outputFormatString);
    return format.format(v);
  };

  /**
   * @param pmAttr
   *          The attribute. Provides the language context.
   *
   * @return The associated number format.<br>
   *         In case of an empty or <code>null</code> result of
   *         {@link PmAttr#getFormatString()}, a {@link DecimalFormat} for the
   *         current locale of the given pmAttr will be returned.
   */
  protected NumberFormat getNumberFormat(PmAttr<?> pmAttr) {
        String formatString = pmAttr.getFormatString();
        Locale locale = pmAttr.getPmConversation().getPmLocale();
        return (StringUtils.isBlank(formatString))

        ? NumberFormat.getNumberInstance(locale)
            : new DecimalFormat(formatString, new DecimalFormatSymbols(locale));
  }

  protected NumberFormat getNumberFormat(Locale locale, String formatString) {
    return (StringUtils.isBlank(formatString))
        ? NumberFormat.getNumberInstance(locale)
        : new DecimalFormat(formatString, new DecimalFormatSymbols(locale));
  }


  /**
   * Implementation of converter capable of handling multiple input formats.
   */
  private MultiFormatParserBase<T> multiFormatParser = new MultiFormatParserBase<T>() {

    @SuppressWarnings("unchecked")
    @Override
    protected T parseValue(String s, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException {
      NumberFormat nf = getNumberFormat(locale, format);

      return (T) nf.parse(s);
    }

    @Override
    protected String getDefaultFormatPattern() {
      return defaultPattern;
    }
  };

  public void setDefaultPattern(String defaultPattern) {
    this.defaultPattern = defaultPattern;
  }

}
