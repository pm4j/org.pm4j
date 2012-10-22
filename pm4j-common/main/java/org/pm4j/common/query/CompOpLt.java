package org.pm4j.common.query;


public class CompOpLt extends CompOpBase<Object> {

  public static final String NAME = "compOpLt";

  public CompOpLt() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

}
