package org.pm4j.common.query;


/**
 * An 'less or equal' compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author olaf boede
 */
public class CompOpLe extends CompOpBase<Object> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpLe";

  public CompOpLe() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Object filterValue) {
    return filterValue != null;
  }

  @Override
  public String toString() {
    return "<=";
  }
}
