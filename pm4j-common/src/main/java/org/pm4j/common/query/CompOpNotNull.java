package org.pm4j.common.query;


/**
 * A NOT EQUALS compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author olaf boede
 */
public class CompOpNotNull extends CompOpBase<Object> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpNotNull";

  public CompOpNotNull() {
    super(NAME);
    setValueNeeded(ValueNeeded.NO);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return true;
  }

  @Override
  public String toString() {
    return "not null";
  }
}
