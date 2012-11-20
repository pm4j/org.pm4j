package org.pm4j.common.query;


/**
 * An IS NULL compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author olaf boede
 */
public class CompOpIsNull extends CompOpBase<Object> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpIsNull";

  public CompOpIsNull() {
    super(NAME);
    setValueNeeded(ValueNeeded.NO);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return true;
  }

  @Override
  public String toString() {
    return "is null";
  }
}
