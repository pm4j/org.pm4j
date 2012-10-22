package org.pm4j.common.query;


public class CompOpEquals extends CompOpBase<Object> {

  public static final String NAME = "compOpEquals";

  public CompOpEquals() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

}
