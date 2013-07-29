package org.pm4j.core.pm.filter.impl;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

public class CompOpNotEquals extends CompOpBase<Object> {

  public static final String NAME = "compOpNotEquals";

  public CompOpNotEquals(PmObject pmCtxt) {
    super(NAME, PmLocalizeApi.localize(pmCtxt, NAME));
  }

  @Override
  protected boolean doesValueMatchImpl(Object itemValue, Object filterValue) {
    return ! ObjectUtils.equals(itemValue, filterValue);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

}
