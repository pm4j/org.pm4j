package org.pm4j.core.pm.filter.impl;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

public class CompOpIsNull extends CompOpBase<Object> {

  public static final String NAME = "compOpIsNull";

  public CompOpIsNull(PmObject pmCtxt) {
    super(NAME, PmLocalizeApi.localize(pmCtxt, NAME));
    setValueNeeded(ValueNeeded.NO);
  }

  @Override
  protected boolean doesValueMatchImpl(Object itemValue, Object filterValue) {
    return itemValue == null;
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return true;
  }

}
