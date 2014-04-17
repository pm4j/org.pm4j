package org.pm4j.common.query;

import java.util.Collection;


/**
 * An IN compare operator.
 * <p>
 * Corresponding evaluators provide technology specific logic.
 *
 * @author Olaf Boede
 */
public class CompOpIn extends CompOpBase<Collection<?>> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpIn";

  public CompOpIn() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(Collection<?> filterValue) {
    return (filterValue != null) &&
           (filterValue.size() > 0);
  }

  @Override
  public String toString() {
    return "in";
  }
}
