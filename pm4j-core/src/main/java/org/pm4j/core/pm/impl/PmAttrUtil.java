package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;

/**
 * Collection of some useful utility methods for {@link PmAttr}.
 *
 * @author Olaf Boede
 */
public class PmAttrUtil {

  /**
   * Sets the backing value of the given attribute(s) to its default value.<br>
   * Please use it with care because this call may fails in case of attributes
   * that are not intended for modification. E.g. in case of calculated
   * attributes that support only read operations.
   * <p>
   * It does not consider attribute enablement!
   *
   * @param attrs
   *          The set of attributes to adjust.
   */
  @SuppressWarnings("unchecked")
  public static void resetBackingValueToDefault(PmAttr<?>... attr) {
    for (PmAttr<?> a : attr) {
      PmAttrBase<Object, Object> ab = (PmAttrBase<Object, Object>) a;
      Object defaultVal = ab.getDefaultValue();
      Object defaultBackingVal = valueToBackingValue(ab, defaultVal);
      ab.setBackingValue(defaultBackingVal);
    }
  }

  /**
   * Calls {@link #resetBackingValueToDefault(PmAttr...)} for all attributes within the given PM tree.
   * <p>
   * If you need more fine grained control you may consider using a customized {@link ResetBackingValueToDefaultVisitorCallback}.
   *
   * @param rootPm The root of the PM tree containing the attributes to reset.
   */
  public static void resetBackingValuesInTreeToDefault(PmObject rootPm) {
    PmVisitorApi.visit(rootPm, new ResetBackingValueToDefaultVisitorCallback(), PmVisitHint.SKIP_CONVERSATION);
  }

  /**
   * Checks if the value is assumed as an empty one for the given attribute.
   * <p>
   * The check is value type specific. E.g. for a numeric attribute is only
   * <code>null</code> empty. But for a list is also am empty list 'empty'.
   *
   * @param pmAttr The attribute to check the value for.
   * @param value The value to check.
   * @return <code>true</code> if the value represents a kind of empty attribute value.
   */
  @SuppressWarnings("unchecked")
  public static boolean isEmptyValue(PmAttr<?> pmAttr, Object value) {
    return ((PmAttrBase<Object, ?>)pmAttr).isEmptyValue(value);
  }

  /**
   * Provides the bound backing value of the given {@link PmAttr}.<br>
   * A utility method that makes the method {@link PmAttrBase#getBackingValue()} available on
   * {@link PmAttr} interface level.
   * <p>
   * PERFORMANCE HINT: Does not use the optionally configured attribute value cache.
   *
   * @param pmAttr The attribute to get the value from.
   * @return The backing value.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getBackingValue(PmAttr<?> pmAttr) {
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
   * Converts between external and internal attribute value.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt
   *          The attribute that provides the converter logic.<br>
   *          If the value conversion is implemented correctly (without
   *          attribute state change) it's value will not be affected.
   * @param intValue
   *          The value to convert. May be <code>null</code>.
   * @return The corresponding external value. May be <code>null</code>.
   */
  public static <T_EXT, T_INT> T_EXT backingValueToValue(PmAttrBase<T_EXT, T_INT> pmAttrCtxt, T_INT intValue) {
    return intValue != null || pmAttrCtxt.isConvertingNullValueImpl()
        ? pmAttrCtxt.convertBackingValueToPmValue(intValue)
        : null;
  }

  /**
   * Converts between external and internal attribute value the same way as the
   * attribute does it internally.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt
   *          The attribute that provides the converter logic.<br>
   *          If the value conversion is implemented correctly (without
   *          attribute state change) it's value will not be affected.
   * @param extValue
   *          The value to convert. May be <code>null</code>.
   * @return The corresponding internal value. May be <code>null</code>.
   */
  public static <T_EXT, T_INT> T_INT valueToBackingValue(PmAttrBase<T_EXT, T_INT> pmAttrCtxt, T_EXT extValue) {
    return extValue != null || pmAttrCtxt.isConvertingNullValueImpl()
        ? pmAttrCtxt.convertPmValueToBackingValue(extValue)
          : null;
  }

  /**
   * Converts between external attribute value and it's corresponding string the
   * same way as the attribute does it internally.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt The attribute that provides the converter logic.
   * @param s The string to convert. May be <code>null</code>.
   * @return The corresponding value. May be <code>null</code>.
   * @throws PmConverterException
   *           if the string does not correspond to a value. E.g. in case of a
   *           format issue.
   */
  public static <T_EXT> T_EXT convertStringToValue(PmAttr<T_EXT> pmAttrCtxt, String s) {
    @SuppressWarnings("unchecked")
    PmAttrBase<T_EXT, ?> a = (PmAttrBase<T_EXT, ?>) pmAttrCtxt;
    try {
      return (s != null || a.isConvertingNullValueImpl())
          ? a.stringToValueImpl(s)
          : null;
    } catch (PmConverterException e) {
      throw new PmRuntimeException(pmAttrCtxt, e.getResourceData(), e);
    }
  }

  /**
   * Converts between external attribute value and it's corresponding string the
   * same way as the attribute does it internally.<br>
   * This call does not change the attribute value!
   *
   * @param pmAttrCtxt The attribute that provides the converter logic.
   * @param value The value to convert to a string. May be <code>null</code>.
   * @return The corresponding string. May be <code>null</code>.
   */
  public static <T_EXT> String convertValueToString(PmAttr<T_EXT> pmAttrCtxt, T_EXT value) {
    @SuppressWarnings("unchecked")
    PmAttrBase<T_EXT, ?> a = (PmAttrBase<T_EXT, ?>) pmAttrCtxt;
    return (value != null || a.isConvertingNullValueImpl())
        ? a.valueToStringImpl(value)
        : null;
  }

  /** @deprecated Please use {@link #backingValueToValue(PmAttrBase, Object)}. */
  @Deprecated
  public static <T_EXT, T_INT> T_EXT convertBackingValue(PmAttrBase<T_EXT, T_INT> pmAttrCtxt, T_INT intValue) {
    return backingValueToValue(pmAttrCtxt, intValue);
  }

  /** @deprecated Please use {@link #valueToBackingValue(PmAttrBase, Object)}. */
  @Deprecated
  public static <T_EXT, T_INT> T_INT convertExternalValue(PmAttrBase<T_EXT, T_INT> pmAttrCtxt, T_EXT extValue) {
    return valueToBackingValue(pmAttrCtxt, extValue);
  }

  /**
   * A visitor callback that resets the backing values of visited attributes to their default value.
   * <p>
   * ATTENTION: Does not consider attribute enablement and will fail for calculated read-only attributes.
   * <p>
   * You may override {@link #resetAttribute(PmAttrBase)} to get more control.
   */
  public static class ResetBackingValueToDefaultVisitorCallback implements PmVisitorApi.PmVisitCallBack {
    @Override
    public PmVisitResult visit(PmObject pm) {
      if (pm instanceof PmAttrBase) {
        resetAttribute((PmAttrBase<?, ?>) pm);
      }
      return PmVisitResult.CONTINUE;
    }

    /**
     * The default implementation
     * @param attrPm
     */
    protected void resetAttribute(PmAttrBase<?, ?> attrPm) {
      PmAttrUtil.resetBackingValueToDefault(attrPm);
    }
  };

}
