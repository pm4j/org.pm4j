package org.pm4j.core.pm.filter.impl;

import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

public class CompOpLt extends CompOpBase<Object> {

  public static final String NAME = "compOpLt";

  public CompOpLt(PmObject pmCtxt) {
    super(NAME, PmLocalizeApi.localize(pmCtxt, NAME));
  }

  @Override
  protected boolean doesValueMatchImpl(Object itemValue, Object filterValue) {
    return CompareUtil.compare((Comparable<?>)itemValue, (Comparable<?>)filterValue) < 0;
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

}
