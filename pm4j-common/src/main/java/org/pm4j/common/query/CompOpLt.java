package org.pm4j.common.query;


/**
 * An LESS THAN compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author Olaf Boede
 */
public class CompOpLt extends CompOpBase<Comparable<?>> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpLt";

  public CompOpLt() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Comparable<?> filterValue) {
    return filterValue != null;
  }

  @Override
  public String toString() {
    return "<";
  }
}
