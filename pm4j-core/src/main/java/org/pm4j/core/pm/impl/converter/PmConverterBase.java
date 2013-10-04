package org.pm4j.core.pm.impl.converter;

import org.pm4j.core.pm.PmAttr;

/**
 * Provides a simple base implementations for {@link #valueToString(PmAttr, Object)}.
 *
 * @author olaf boede
 *
 * @param <T> Type of the value to convert.
 */
public abstract class PmConverterBase<T> implements PmAttr.Converter<T> {

  /**
   * Just calls the {@link #toString()} method of the given value.
   * <p>
   * This solution works if the {@link #toString()} implementation provides the
   * information needed for the {@link #stringToValue(PmAttr, String)} method
   * call.
   */
  @Override
  public String valueToString(PmAttr<?> pmAttr, T v) {
    return v.toString();
  }

}
