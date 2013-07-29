package org.pm4j.core.pm.filter.impl;

import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

public class CompOpStringEquals extends CompOpStringBase {

  public static final String NAME = "compOpStringEquals";

  public CompOpStringEquals(PmObject pmCtxt) {
    super(NAME, PmLocalizeApi.localize(pmCtxt, NAME));
  }

  @Override
  protected boolean doesValueMatchImpl(String itemValue, String filterValue) {
    return CompareUtil.equalStrings(itemValue, filterValue, isIgnoreCase(), isIgnoreSpaces());
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return true;
  }

}
