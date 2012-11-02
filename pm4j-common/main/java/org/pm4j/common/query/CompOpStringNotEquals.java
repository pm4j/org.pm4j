package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;


public class CompOpStringNotEquals extends CompOpStringBase {

  public static final String NAME = "compOpStringNotEquals";

  public CompOpStringNotEquals() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return ! StringUtils.isBlank(filterValue);
  }

}
