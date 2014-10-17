package org.pm4j.deprecated.core.pm.filter.impl;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrUtil;
import org.pm4j.deprecated.core.pm.filter.DeprCompOp;

/**
 * A filter that converts the entered filter-by value to the attribute value type
 * before applying the selected {@link DeprCompOp}.
 * <p>
 * This kind of filter is useful for {@link Comparable} values like numbers.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprFilterByPmAttrValue extends DeprFilterByPmAttrBase<Object> {

  public static Class<?>[] DEFAULT_COMP_OPS = {
    DeprCompOpEquals.class,
    DeprCompOpGt.class,
    DeprCompOpLt.class,
    DeprCompOpNotNull.class,
  };

  public DeprFilterByPmAttrValue(PmObject pmCtxt, Class<?>... compOpClasses) {
    super(pmCtxt, compOpClasses);
  }

  public DeprFilterByPmAttrValue(PmObject pmCtxt) {
    this(pmCtxt, DEFAULT_COMP_OPS);
  }

  @Override
  protected boolean doesItemMatchImpl(PmAttr<?> pmAttr, DeprCompOp compOp, Object filterValue) {
    try {
      Object filterObj = (filterValue instanceof String)
            ? PmAttrUtil.convertStringToValue(pmAttr, (String)filterValue)
            : filterValue;
      return compOp.doesValueMatch(pmAttr.getValue(), filterObj);
    } catch (PmRuntimeException e) {
      return showItemsInCaseOfFilterValueConverterFailure(compOp, filterValue);
    }
  }

  /**
   * The default implementation returns here <code>true</code>.<br>
   * That means: If the user entered a filter value that can't be converted to
   * the required type, no item will be shown.
   * <p>
   * XXX olaf:<br>
   * That might match in most cases, because there is no item that can have the
   * entered value.<br>
   * On the other hand it may be useful that the Not-filters return
   * <code>true</code>.
   *
   * @param compOp
   *          The user defined compare operator.
   * @param unConvertedFilterValue
   *          The unconverted filter field value.
   * @return <code>true</code> to show the item with the given condition.
   */
  protected boolean showItemsInCaseOfFilterValueConverterFailure(DeprCompOp compOp, Object unConvertedFilterValue) {
    return false;
  }

}
