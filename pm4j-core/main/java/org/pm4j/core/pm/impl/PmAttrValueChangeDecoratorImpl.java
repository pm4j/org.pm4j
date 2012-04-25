package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttr.ValueChangeCommand;

public class PmAttrValueChangeDecoratorImpl<T_VALUE> extends PmCommandDecoratorBase<PmAttr.ValueChangeCommand<T_VALUE>> {

  @Override
  protected boolean beforeDoImpl(ValueChangeCommand<T_VALUE> cmd) {
    return beforeChange(cmd.getPmAttr(), cmd.getOldValue(), cmd.getNewValue());
  }

  @Override
  protected void afterDoImpl(ValueChangeCommand<T_VALUE> cmd) {
    afterChange(cmd.getPmAttr(), cmd.getOldValue(), cmd.getNewValue());
  }

  protected boolean beforeChange(PmAttr<T_VALUE> pmAttr, T_VALUE oldValue, T_VALUE newValue) {
    return true;
  }

  protected void afterChange(PmAttr<T_VALUE> pmAttr, T_VALUE oldValue, T_VALUE newValue) {
  }

}
