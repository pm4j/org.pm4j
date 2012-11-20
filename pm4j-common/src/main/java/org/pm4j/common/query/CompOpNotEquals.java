package org.pm4j.common.query;


/**
 * An NOT EQUALS compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author olaf boede
 */
public class CompOpNotEquals extends CompOpBase<Object> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpNotEquals";

  public CompOpNotEquals() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

  @Override
  public String toString() {
    return "!=";
  }
}
