package org.pm4j.common.query;


public class CompOpStringEquals extends CompOpStringBase {

  public static final String NAME = "compOpStringEquals";

  public CompOpStringEquals() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return true;
  }

}
