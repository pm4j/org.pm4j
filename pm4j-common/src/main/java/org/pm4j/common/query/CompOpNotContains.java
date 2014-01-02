package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;

public class CompOpNotContains extends CompOpBase<String> {

  private static final long serialVersionUID = 1L;

  public static final String NAME = "compOpStringNotContains";

  public CompOpNotContains() {
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
