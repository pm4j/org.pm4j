package org.pm4j.core.pm.impl.converter;

import java.io.Serializable;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttr;

/**
 * Base class for converters the check the actual value type if it's {@link Serializable}.
 * In this case the value will be serialized directly. Otherwise the string representation
 * of the attribute is used for serialization.
 *
 * @author Olaf Boede
 *
 * @param <T> The value type.
 */
abstract class CopyOfPmConverterBase<T> implements PmAttr.Converter<T> {

  /**
   * Provides directly the {@link #getValue()} result if it is
   * {@link Serializable}.
   * <p>
   * Provides the result of {@link #getValueAsString()} if the
   * {@link #getValue()} result is not {@link Serializable}.
   */
  public Serializable valueToSerializable(PmAttr<?> pmAttr, T v) {
    return (v == null) || (v instanceof Serializable)
              ? (Serializable) v
              : valueToString(pmAttr, v);
  }

  /**
   * Uses {@link #stringToValue(PmAttr, String)} if the provided value is a
   * {@link String}.<br>
   * Casts the given value to <code>T</code> if the provided value is not a
   * {@link String}.
   * @throws PmValidationException
   */
  @SuppressWarnings("unchecked")
  @Override
  public T serializeableToValue(PmAttr<?> pmAttr, Serializable s) throws PmConverterException {
    if ((s != null) && s.getClass().equals(String.class)) {
      return stringToValue(pmAttr, (String) s);
    } else {
      return (T) s;
    }
  }
}
