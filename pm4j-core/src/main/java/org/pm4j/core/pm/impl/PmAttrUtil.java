package org.pm4j.core.pm.impl;

import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.core.pm.impl.PmAttrBase.MetaData;

/**
 * Collection of some useful utility methods for {@link PmAttr}.
 *
 * @author Olaf Boede
 */
public class PmAttrUtil {

  /**
   * Sets the backing value of the given attribute(s) to its default value.<br>
   * Please use it with care because this call may fail in case of attributes
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
      if (isBackingValueWriteable(ab)) {
        Object defaultVal = ab.getDefaultValue();
        Object defaultBackingVal = valueToBackingValue(ab, defaultVal);
        if (defaultBackingVal == null && getMetaData(ab).primitiveType) {
            throw new PmRuntimeException(ab, "Can't assign a null default value to a primitive value type. Please define a valid default value.");
          }
        ab.setBackingValue(defaultBackingVal);
      }
    }
  }

  /**
   * Calls {@link #resetBackingValueToDefault(PmAttr...)} for all attributes within the given PM tree.
   * <p>
   * If you need more fine grained control you may consider using a customized {@link ResetBackingValueToDefaultVisitorCallback}.
   *
   * @param rootPm The root of the PM tree containing the attributes to reset.
   */
  public static void resetBackingValuesToDefault(PmObject rootPm) {
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
   * @param backingValue
   *          The value to convert. May be <code>null</code>.
   * @return The corresponding external value. May be <code>null</code>.
   */
  public static <T_VALUE, T_BACKING_VALUE> T_VALUE backingValueToValue(PmAttrBase<T_VALUE, T_BACKING_VALUE> pmAttrCtxt, T_BACKING_VALUE backingValue) {
    return backingValue != null || pmAttrCtxt.isConvertingNullValueImpl()
        ? pmAttrCtxt.convertBackingValueToPmValue(backingValue)
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
   * @param value
   *          The value to convert. May be <code>null</code>.
   * @return The corresponding internal value. May be <code>null</code>.
   */
  public static <T_VALUE, T_BACKING_VALUE> T_BACKING_VALUE valueToBackingValue(PmAttrBase<T_VALUE, T_BACKING_VALUE> pmAttrCtxt, T_VALUE value) {
    return value != null || pmAttrCtxt.isConvertingNullValueImpl()
        ? pmAttrCtxt.convertPmValueToBackingValue(value)
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
  public static <T_VALUE> T_VALUE convertStringToValue(PmAttr<T_VALUE> pmAttrCtxt, String s) {
    @SuppressWarnings("unchecked")
    PmAttrBase<T_VALUE, ?> ab = (PmAttrBase<T_VALUE, ?>) pmAttrCtxt;
    try {
      return (s != null || ab.isConvertingNullValueImpl())
          ? ab.stringToValueImpl(s)
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
  public static <T_VALUE> String convertValueToString(PmAttr<T_VALUE> pmAttrCtxt, T_VALUE value) {
    @SuppressWarnings("unchecked")
    PmAttrBase<T_VALUE, ?> ab = (PmAttrBase<T_VALUE, ?>) pmAttrCtxt;
    return (value != null || ab.isConvertingNullValueImpl())
        ? ab.valueToStringImpl(value)
        : null;
  }

  /** @deprecated Please use {@link #backingValueToValue(PmAttrBase, Object)}. */
  @Deprecated
  public static <T_VALUE, T_BACKING_VALUE> T_VALUE convertBackingValue(PmAttrBase<T_VALUE, T_BACKING_VALUE> pmAttrCtxt, T_BACKING_VALUE backingValue) {
    return backingValueToValue(pmAttrCtxt, backingValue);
  }

  /** @deprecated Please use {@link #valueToBackingValue(PmAttrBase, Object)}. */
  @Deprecated
  public static <T_VALUE, T_BACKING_VALUE> T_BACKING_VALUE convertExternalValue(PmAttrBase<T_VALUE, T_BACKING_VALUE> pmAttrCtxt, T_VALUE value) {
    return valueToBackingValue(pmAttrCtxt, value);
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

  /**
   * Checks if it is physically possible to write the backing attribute value.<br>
   * That may be evaluated for the value access strategies 'override' and 'reflection'.<br>
   * TODO oboede:
   * It currently can't be determined for the strategies 'localValue' and 'valuePath'.
   *
   * @param pmAttr The attribute to check
   * @return <code>true</code> if a write operation most likely works.
   */
  static boolean isBackingValueWriteable(PmAttrBase<?,?> pmAttr) {
    MetaData md = getMetaData(pmAttr);
    if (md.valueAccessStrategy == PmAttrBase.ValueAccessOverride.INSTANCE) {
      // XXX oboede: may be optimized it it gets a problem.
      return ClassUtil.findMethods(pmAttr.getClass(), "setBackingValueImpl").size() > 1;
    }
    if (md.beanAttrAccessor != null) {
      return md.beanAttrAccessor.canSet();
    }
    // No physical write access restriction for other strategies implemented.
    return true;
  }

  private static MetaData getMetaData(PmAttrBase<?,?> pmAttr) {
    return (MetaData) pmAttr.getPmMetaData();
  }
}
