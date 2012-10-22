package org.pm4j.common.query;


public class CompOpStringIsEmpty extends CompOpStringBase {

  public static final String NAME = "compOpStringIsEmpty";

  public CompOpStringIsEmpty() {
    super(NAME);
    setValueNeeded(ValueNeeded.NO);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return true;
  }

}
