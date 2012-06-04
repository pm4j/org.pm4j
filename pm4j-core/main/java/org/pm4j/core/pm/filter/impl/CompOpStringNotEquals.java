package org.pm4j.core.pm.filter.impl;

import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

public class CompOpStringNotEquals extends CompOpStringBase {

  public static final String NAME = "compOpStringNotEquals";

  public CompOpStringNotEquals(PmObject pmCtxt) {
    super(NAME, PmLocalizeApi.localize(pmCtxt, NAME));
  }

  @Override
  protected boolean doesValueMatchImpl(String itemValue, String filterValue) {
    return ! CompareUtil.equalStrings(itemValue, filterValue, isIgnoreCase(), isIgnoreSpaces());
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return true;
  }

}
