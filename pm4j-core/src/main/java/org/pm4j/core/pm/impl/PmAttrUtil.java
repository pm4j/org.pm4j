package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;

/**
 * Collection of some useful utility methods for {@link PmAttr}.
 *
 * @author olaf boede
 */
public class PmAttrUtil {

  /**
   * Checks if the value is assumed as an empty one for the given attribute.
   * <p>
   * The check is value type specific. E.g. for a numeric attribute is only <code>null</code>
   * empty. But for a list is also am empty list 'empty'.
   *
   * @param pmAttr
   * @param value
   * @return
   */
  @SuppressWarnings("unchecked")
  public static boolean isEmptyValue(PmAttr<?> pmAttr, Object value) {
    return ((PmAttrBase<Object, ?>)pmAttr).isEmptyValue(value);
  }

  /**
   * Provides the backing value of the given {@link PmAttr}.
   *
   * @param pmAttr The attribute to get the value from.
   * @return The backing value.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getBackingValue(PmAttr<?> pmAttr) {
    //
    if (pmAttr instanceof PmAttrBase) {
      return ((PmAttrBase<?, T>)pmAttr).getBackingValue();
    } else if (pmAttr instanceof PmAttrProxy) {
      PmAttr<?> delegate = ((PmAttrProxy<?>)pmAttr).getDelegate();
      return (delegate != null)
          ? (T) getBackingValue(delegate)
          : null;
    } else {
      throw new PmRuntimeException(pmAttr, "Unable to handle attribute type: " + pmAttr.getClass());
    }
  }

}
