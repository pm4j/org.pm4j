package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;


public class CompOpStringEquals extends CompOpStringBase {

  public static final String NAME = "compOpStringEquals";

  public CompOpStringEquals() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return ! StringUtils.isBlank(filterValue);
  }

}
