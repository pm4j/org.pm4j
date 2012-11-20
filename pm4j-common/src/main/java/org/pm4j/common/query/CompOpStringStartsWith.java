package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;

public class CompOpStringStartsWith extends CompOpBase<String> {

  private static final long serialVersionUID = 1L;

  public static final String NAME = "compOpStringStartsWith";

  public CompOpStringStartsWith() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return ! StringUtils.isBlank(filterValue);
  }

  @Override
  public String toString() {
    return "startsWith";
  }
}
