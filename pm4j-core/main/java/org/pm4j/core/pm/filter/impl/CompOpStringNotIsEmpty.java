package org.pm4j.core.pm.filter.impl;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

public class CompOpStringNotIsEmpty extends CompOpStringBase {

  public static final String NAME = "compOpStringNotIsEmpty";

  public CompOpStringNotIsEmpty(PmObject pmCtxt) {
    super(NAME, PmLocalizeApi.localize(pmCtxt, NAME));
  }

  @Override
  protected boolean doesValueMatchImpl(String itemValue, String filterValue) {
    return ! StringUtils.isEmpty(itemValue);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return true;
  }

}
