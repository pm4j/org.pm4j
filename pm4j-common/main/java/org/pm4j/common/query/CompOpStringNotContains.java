package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;

public class CompOpStringNotContains extends CompOpStringBase {

  public static final String NAME = "compOpStringNotContains";

  public CompOpStringNotContains() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return ! StringUtils.isBlank(filterValue);
  }

  @Override
  public String toString() {
    return "not contains";
  }
}
