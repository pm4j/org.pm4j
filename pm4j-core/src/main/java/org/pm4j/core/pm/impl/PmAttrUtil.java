package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmConverterException;
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

  /**
   * Converts between external and internal attribute value the same way as the attribute
   * does it internally.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt The attribute that provides the converter logic.
   * @param intValue The value to convert. May be <code>null</code>.
   * @return The corresponding external value. May be <code>null</code>.
   */
  public static <T_EXT, T_INT> T_EXT convertBackingValue(PmAttrBase<T_EXT, T_INT> pmAttrCtxt, T_INT intValue) {
    // TODO oboede: switch to converter call asap.
    return intValue != null ? pmAttrCtxt.convertBackingValueToPmValue(intValue) : null;
    // return pmAttrCtxt.getValueConverter().toExternalValue(pmAttrCtxt.getConverterCtxt(), intValue);
  }

  /**
   * Converts between external and internal attribute value the same way as the attribute
   * does it internally.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt The attribute that provides the converter logic.
   * @param extValue The value to convert. May be <code>null</code>.
   * @return The corresponding internal value. May be <code>null</code>.
   */
  public static <T_EXT, T_INT> T_INT convertExternalValue(PmAttrBase<T_EXT, T_INT> pmAttrCtxt, T_EXT extValue) {
    // TODO oboede: switch to converter call asap.
    return extValue != null ? pmAttrCtxt.convertPmValueToBackingValue(extValue) : null;
    // return pmAttrCtxt.getValueConverter().toInternalValue(pmAttrCtxt.getConverterCtxt(), extValue);
  }

  /**
   * Converts between external attribute value and it's corresponding string the same way as the attribute
   * does it internally.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt The attribute that provides the converter logic.
   * @param s The string to convert. May be <code>null</code>.
   * @return The corresponding value. May be <code>null</code>.
   * @throws PmConverterException if the string does not correspond to a value. E.g. in case of a format issue.
   */
  public static <T_EXT> T_EXT convertStringToValue(PmAttr<T_EXT> pmAttrCtxt, String s) throws PmConverterException {
    @SuppressWarnings("unchecked")
    PmAttrBase<T_EXT, ?> a = (PmAttrBase<T_EXT, ?>) pmAttrCtxt;
    // TODO oboede: switch to converter call asap.
    return (s != null) ? a.stringToValueImpl(s) : null;
//    try {
//      return a.getStringConverter().stringToValue(a.getConverterCtxt(), s);
//    } catch (StringConverterParseException e) {
//      throw new PmConverterException(a, e);
//    }
  }

  /**
   * Converts between external attribute value and it's corresponding string the same way as the attribute
   * does it internally.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt The attribute that provides the converter logic.
   * @param value The value to convert to a string. May be <code>null</code>.
   * @return The corresponding string. May be <code>null</code>.
   */
  public static <T_EXT> String convertValueToString(PmAttr<T_EXT> pmAttrCtxt, T_EXT value) {
    @SuppressWarnings("unchecked")
    PmAttrBase<T_EXT, ?> a = (PmAttrBase<T_EXT, ?>) pmAttrCtxt;
    // TODO oboede: switch to converter call asap.
    return value != null ? a.valueToStringImpl(value) : null;
//    return a.getStringConverter().valueToString(a.getConverterCtxt(), value);
  }

}
