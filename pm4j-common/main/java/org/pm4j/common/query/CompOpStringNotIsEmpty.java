package org.pm4j.common.query;


public class CompOpStringNotIsEmpty extends CompOpStringBase {

  public static final String NAME = "compOpStringNotIsEmpty";

  public CompOpStringNotIsEmpty() {
    super(NAME);
    setValueNeeded(ValueNeeded.NO);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return true;
  }

  @Override
  public String toString() {
    return "not empty";
  }
}
