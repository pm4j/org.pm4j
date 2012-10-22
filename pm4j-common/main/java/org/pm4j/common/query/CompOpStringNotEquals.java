package org.pm4j.common.query;


public class CompOpStringNotEquals extends CompOpStringBase {

  public static final String NAME = "compOpStringNotEquals";

  public CompOpStringNotEquals() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return true;
  }

}
