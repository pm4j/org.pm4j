package org.pm4j.common.query;


public class CompOpNotEquals extends CompOpBase<Object> {

  public static final String NAME = "compOpNotEquals";

  public CompOpNotEquals() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

}
