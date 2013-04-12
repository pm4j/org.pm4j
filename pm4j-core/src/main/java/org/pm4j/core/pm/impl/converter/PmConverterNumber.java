 package org.pm4j.core.pm.impl.converter;

import java.lang.reflect.Constructor;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
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
public class PmConverterNumber<T extends Number> extends PmConverterSerializeableBase<T> implements MultiFormatConverter {

  private final Constructor<T> numberCtor;

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
      T value = multiFormatParser.parseString(pmAttr, s);
      if (value != null && value.getClass() != numberCtor.getDeclaringClass()) {
        return numberCtor.newInstance(value.toString());
      }
      else {
        return value;
      }

    } catch (Exception e) {
      throw new PmResourceRuntimeException(pmAttr, PmConstants.MSGKEY_VALIDATION_NUMBER_CONVERSION_FROM_STRING_FAILED, pmAttr.getPmTitle());
    }
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, T v) {
    String outputFormatString = multiFormatParser.getOutputFormat(pmAttr);
    NumberFormat format = getNumberFormat(pmAttr.getPmConversation().getPmLocale(), outputFormatString, pmAttr);
    return format.format(v);
  };

  @Override
  public String getOutputFormat(PmAttr<?> pmAttr) {
    return multiFormatParser.getOutputFormat(pmAttr);
  }

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

  protected NumberFormat getNumberFormat(Locale locale, String formatString, PmAttr<?> pmAttr) {
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
      NumberFormat nf = getNumberFormat(locale, format, pmAttr);
      if(nf instanceof DecimalFormat) {
        DecimalFormat decimalFormat = (DecimalFormat) nf;
        ParsePosition parsePosition = new ParsePosition(0);
        Object object = decimalFormat.parse(s, parsePosition);
        // make sure that the whole string matches
        if( parsePosition.getIndex() < s.length() ) {
          throw new ParseException("input string does only match in parts", parsePosition.getIndex());
        }
        // make sure that max and min fraction match
        try {
          nf.setRoundingMode(RoundingMode.UNNECESSARY);
          nf.format(object);
        } catch (ArithmeticException e) {
          throw new ParseException(e.getMessage(), 0);
        }

        return (T) object;
      } else {
        return (T) nf.parse(s);
      }

    }
  };


}
