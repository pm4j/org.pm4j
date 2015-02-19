package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttr.ValueChangeCommand;

/**
 * A decorator with special support for attribute value change handling.
 * <p>
 * It may be registered by calling {@link PmAttr#addValueChangeDecorator(org.pm4j.core.pm.PmCommandDecorator)}.
 *
 * @param <T_VALUE> The (external) attribute value type.
 *
 * @author olaf boede
 */
public class PmAttrValueChangeDecorator<T_VALUE> extends PmCommandDecoratorBase<PmAttr.ValueChangeCommand<T_VALUE>> {

  /**
   * May be overridden to implement UI logic to be called before a value change
   * happens.
   *
   * @param pmAttr
   *          The attribute to change.
   * @param oldValue
   *          The old (current) attribute value.
   * @param newValue
   *          The new attribute value to set.
   * @return <code>false</code> prevents the value change.<br>
   *         <code>true</code> allows the value change.
   */
  protected boolean beforeChange(PmAttr<T_VALUE> pmAttr, T_VALUE oldValue, T_VALUE newValue) {
    return true;
  }

  /**
   * May be overridden to implement UI logic to be called after each value change.
   *
   * @param pmAttr
   *          The attribute to change.
   * @param oldValue
   *          The old attribute value.
   * @param newValue
   *          The new (current) attribute value.
   */
  protected void afterChange(PmAttr<T_VALUE> pmAttr, T_VALUE oldValue, T_VALUE newValue) {
  }

  @Override
  protected boolean beforeDoImpl(ValueChangeCommand<T_VALUE> cmd) {
    return beforeChange(cmd.getPmAttr(), cmd.getOldValue(), cmd.getNewValue());
  }

  @Override
  protected void afterDoImpl(ValueChangeCommand<T_VALUE> cmd) {
    afterChange(cmd.getPmAttr(), cmd.getOldValue(), cmd.getNewValue());
  }

}
