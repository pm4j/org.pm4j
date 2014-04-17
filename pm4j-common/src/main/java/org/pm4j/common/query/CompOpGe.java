package org.pm4j.common.query;


/**
 * An 'grater than or equal' compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author Olaf Boede
 */
public class CompOpGe extends CompOpBase<Comparable<?>> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpGe";

  public CompOpGe() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Comparable<?> filterValue) {
    return filterValue != null;
  }

  @Override
  public String toString() {
    return ">=";
  }
}
