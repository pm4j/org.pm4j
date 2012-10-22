package org.pm4j.common.query;


public class CompOpNotNull extends CompOpBase<Object> {

  public static final String NAME = "compOpNotNull";

  public CompOpNotNull() {
    super(NAME);
    setValueNeeded(ValueNeeded.NO);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return true;
  }

}
