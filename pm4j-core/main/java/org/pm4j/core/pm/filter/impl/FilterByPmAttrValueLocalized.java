package org.pm4j.core.pm.filter.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.filter.CompOp;
import org.pm4j.core.pm.impl.PmAttrStringImpl;

/**
 * A filter that uses the <code>valueLocalized</code> string of the item
 * attributes for filtering.
 * <p>
 * This is useful for string attributes and other types that just should be
 * filtered by the localized string that is visible for the table user.
 *
 * @author olaf boede
 */
public class FilterByPmAttrValueLocalized extends FilterByPmAttrBase<String> {

  public static Class<?>[] DEFAULT_COMP_OPS = {
    CompOpStringContains.class,
    CompOpStringStartsWith.class,
    CompOpStringEndsWith.class,
    CompOpStringEquals.class,
    CompOpStringNotContains.class,
    CompOpStringNotEquals.class
  };

  public FilterByPmAttrValueLocalized(PmObject pmCtxt, Class<?>... compOpClasses) {
    super(pmCtxt, compOpClasses);
    setValueAttrPmClass(PmAttrStringImpl.class);
  }

  public FilterByPmAttrValueLocalized(PmObject pmCtxt) {
    this(pmCtxt, DEFAULT_COMP_OPS);
  }

  @Override
  public Class<?> getValueType() {
    return String.class;
  }

  @Override
  protected boolean doesItemMatchImpl(PmAttr<?> pmAttr, CompOp compOp, String filterValue) {
    return compOp.doesValueMatch(pmAttr.getValueLocalized(), filterValue);
  }

}
