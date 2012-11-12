package org.pm4j.common.query;

import java.util.Collection;


/**
 * An IN compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author olaf boede
 */
public class CompOpIn extends CompOpBase<Object> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpIn";

  public CompOpIn() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return (filterValue instanceof Collection) &&
           ((Collection<?>)filterValue).size() > 0;
  }

  @Override
  public String toString() {
    return "in";
  }
}
