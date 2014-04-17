package org.pm4j.common.query;


/**
 * An GT compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author Olaf Boede
 */
public class CompOpGt extends CompOpBase<Comparable<?>> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpGt";

  public CompOpGt() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Comparable<?> filterValue) {
    return filterValue != null;
  }

  @Override
  public String toString() {
    return ">";
  }
}
