package org.pm4j.core.pm;

public interface PmAttrNumber<T extends Number> extends PmAttr<T> {

  /**
   * @return The maximum value for this attribute.
   */
  T getMax();

  /**
   * @return The minimum value for this attribute.
   */
  T getMin();

}
