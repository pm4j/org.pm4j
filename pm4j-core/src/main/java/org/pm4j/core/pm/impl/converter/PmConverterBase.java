package org.pm4j.core.pm.impl.converter;

import java.io.Serializable;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttr.Converter;

/**
 * Base class for converters that handle {@link Serializable} values.
 * <p>
 * Provides simple base implementations for some {@link Converter} methods.
 *
 * @author olaf boede
 *
 * @param <T> Type of the value to convert.
 */
public abstract class PmConverterBase<T extends Serializable> implements PmAttr.Converter<T> {

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
