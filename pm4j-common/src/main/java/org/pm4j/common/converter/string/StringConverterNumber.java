 package org.pm4j.common.converter.string;

import java.lang.reflect.Constructor;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.converter.value.ValueConverterCtxtNumber;

/**
 * Base class for number type converters.
 *
 * @param <T> The concrete {@link Number} type to convert.
 *
 * @author Olaf Boede
 */
public class StringConverterNumber<T extends Number> extends StringConverterBase<T, StringConverterCtxt> {

  private final Constructor<T> numberCtor;

  public StringConverterNumber(Class<T> numberClass) {
        try {
          numberCtor = numberClass.getConstructor(String.class);
        } catch (Exception e) {
          throw new RuntimeException("Number class without string constructor is not supported. Class: " + numberClass);
    }

  }

  @Override
  protected T stringToValueImpl(StringConverterCtxt ctxt, String s) throws Exception {
    if (s == null || s.isEmpty()) {
      return null;
    }

    T value = multiFormatParser.parseString(ctxt, s);

    // The DecimalFormat parser delivers only Long or Double values.
    // The following code converts in case of a mismatch to the correct type.
    if (value != null && value.getClass() != numberCtor.getDeclaringClass()) {
      return numberCtor.newInstance(value.toString());
    }
    else {
      return value;
    }
  }

  @Override
  protected String valueToStringImpl(StringConverterCtxt ctxt, T v) {
    if (v == null) {
      return null;
    }

    String outputFormatString = null;
    try {
      outputFormatString = StringConverterUtil.getOutputFormat(ctxt);
      NumberFormat format = getNumberFormat(ctxt, outputFormatString);
      return format.format(v);
    }
    catch (Exception e) {
      // Coding error, output format limits more than the input format.
      throw new RuntimeException("Unable to apply '" + v + "' to format: '" + outputFormatString + "'. Context: " + ctxt, e);
    }
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
  protected NumberFormat getNumberFormat(StringConverterCtxt ctxt, String formatString) {
    Locale locale = ctxt.getConverterCtxtLocale();
    NumberFormat nf = (StringUtils.isBlank(formatString))
        ? NumberFormat.getNumberInstance(locale)
        : new DecimalFormat(formatString, new DecimalFormatSymbols(locale));
    nf.setRoundingMode(getRoundingMode(ctxt));
    return nf;
  }

  protected RoundingMode getRoundingMode(StringConverterCtxt ctxt) {
    return (ctxt instanceof ValueConverterCtxtNumber)
        ? ((ValueConverterCtxtNumber)ctxt).getConverterCtxtRoundingMode()
        : RoundingMode.UNNECESSARY;
  }


  /**
   * Implementation of converter capable of handling multiple input formats.
   */
  private MultiFormatParserBase<T> multiFormatParser = new MultiFormatParserBase<T>() {

    @SuppressWarnings("unchecked")
    @Override
    protected T parseValue(StringConverterCtxt ctxt, String s, String format) throws ParseException {
      NumberFormat nf = getNumberFormat(ctxt, format);
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
