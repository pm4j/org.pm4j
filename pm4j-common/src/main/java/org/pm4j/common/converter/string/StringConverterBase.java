package org.pm4j.common.converter.string;

import java.util.Locale;
import java.util.TimeZone;


/**
 * Provides common logic that may be used to implement type specific converters.
 *
 * @param <T>
 *          Type of the value to convert.
 * @param <C>
 *          Concrete type of string converter context.<br>
 *          Some converters need to access specific context information. The
 *          generic type <code>C</code> supports such cases.
 *
 * @author Olaf Boede
 */
public abstract class StringConverterBase<T, C extends StringConverterCtxt> implements StringConverter<T> {

  @SuppressWarnings("unchecked")
  @Override
  public final String valueToString(StringConverterCtxt ctxt, T v) {
    return valueToStringImpl((C) ctxt, v);
  }

  /**
   * Calls {@link #stringToValueImpl(StringConverterCtxt, String)} and supports
   * exception translation.
   */
  @SuppressWarnings("unchecked")
  @Override
  public final T stringToValue(StringConverterCtxt ctxt, String s) throws StringConverterParseException {
    try {
      return stringToValueImpl((C)ctxt, s);
    } catch (StringConverterParseException e) {
      throw e;
    } catch (Throwable e) {
      throw ctxt.createStringConverterParseException(s, e, StringConverterUtil.getParseFormats(ctxt));
    }
  }

  /**
   * Specific string parse implementation.
   *
   * @param ctxt Provides context information about formats, {@link Locale} and {@link TimeZone}.
   * @param s The string to convert.
   * @return The converted value.
   * @throws Exception
   *           if the string can't be converted. If the exception is a
   *           {@link StringConverterParseException} it will be propagated to
   *           the calling context. If it's a different exception,
   *           {@link #stringToValue(StringConverterCtxt, String)} will wrap it
   *           in a {@link StringConverterParseException}.
   */
  protected abstract T stringToValueImpl(C ctxt, String s) throws Exception;

  /**
   * The default implementation just calls the {@link #toString()} method of the given value.
   */
  protected String valueToStringImpl(C ctxt, T v) {
    return (v != null)
        ? v.toString()
        : null;
  }

}
