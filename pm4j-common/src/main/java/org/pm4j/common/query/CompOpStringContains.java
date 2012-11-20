package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;

public class CompOpStringContains extends CompOpBase<String> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpStringContains";

  public CompOpStringContains() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return ! StringUtils.isBlank(filterValue);
  }

  @Override
  public String toString() {
    return "contains";
  }
}
