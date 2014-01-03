package org.pm4j.deprecated.core.pm.filter.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.deprecated.core.pm.filter.DeprCompOp;

/**
 * A filter that uses the <code>valueLocalized</code> string of the item
 * attributes for filtering.
 * <p>
 * This is useful for string attributes and other types that just should be
 * filtered by the localized string that is visible for the table user.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprFilterByPmAttrValueLocalized extends DeprFilterByPmAttrBase<String> {

  public static Class<?>[] DEFAULT_COMP_OPS = {
    DeprCompOpStringContains.class,
    DeprCompOpStringStartsWith.class,
    DeprCompOpStringEndsWith.class,
    DeprCompOpStringEquals.class,
    DeprCompOpStringNotContains.class,
    DeprCompOpStringNotEquals.class
  };

  public DeprFilterByPmAttrValueLocalized(PmObject pmCtxt, Class<?>... compOpClasses) {
    super(pmCtxt, compOpClasses);
    setValueAttrPmClass(PmAttrStringImpl.class);
  }

  public DeprFilterByPmAttrValueLocalized(PmObject pmCtxt) {
    this(pmCtxt, DEFAULT_COMP_OPS);
  }

  @Override
  protected boolean doesItemMatchImpl(PmAttr<?> pmAttr, DeprCompOp compOp, String filterValue) {
    return compOp.doesValueMatch(pmAttr.getValueLocalized(), filterValue);
  }

}
