package org.pm4j.common.query;


/**
 * An 'grater than or equal' compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author olaf boede
 */
public class CompOpGe extends CompOpBase<Object> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpGe";

  public CompOpGe() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

  @Override
  public String toString() {
    return ">=";
  }
}
