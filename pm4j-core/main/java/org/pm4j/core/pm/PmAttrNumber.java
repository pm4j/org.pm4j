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
  
  /**
   * FIXME olaf: should be an interface for all attributes that may be represented as text (--> PmAttrBase).
   * 
   * @return The maximum field length for representation as string.
   */
  int getMaxLen();

}
