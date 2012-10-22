package org.pm4j.common.query;


public class CompOpGt extends CompOpBase<Object> {

  public static final String NAME = "compOpGt";

  public CompOpGt() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

}
