package org.pm4j.common.query;


public class CompOpIsNull extends CompOpBase<Object> {

  public static final String NAME = "compOpIsNull";

  public CompOpIsNull() {
    super(NAME);
    setValueNeeded(ValueNeeded.NO);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return true;
  }

}
