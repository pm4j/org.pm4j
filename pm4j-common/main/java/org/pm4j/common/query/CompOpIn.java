package org.pm4j.common.query;

import java.util.Collection;


public class CompOpIn extends CompOpBase<Object> {

  public static final String NAME = "compOpIn";

  public CompOpIn() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return (filterValue instanceof Collection) &&
           ((Collection<?>)filterValue).size() > 0;
  }

}
