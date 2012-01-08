package org.pm4j.core.pm.impl.converter;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;

/**
 * Base class for number type converters.
 *
 * @author olaf boede
 *
 * @param <T> The concrete {@link Number} type to convert.
 */
public class PmConverterNumber<T extends Number> extends PmConverterSerializeableBase<T>{

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
      return numberCtor.newInstance(s.trim());
    } catch (Exception e) {
      throw new PmRuntimeException(pmAttr, "Unable to convert string '" + s + "'.", e);
    }
  }

  /**
   * @param pmAttr
   *          The attribute. Provides the language context.
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


}
