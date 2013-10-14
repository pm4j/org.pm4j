package org.pm4j.common.query.inmem;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.CompOpEvaluator;

/**
 * Basic interface for in-memory evaluators.
 * <p>
 * Provides an {@link #equals(Object)} method signature to check the condition.
 *
 * @author olaf boede
 */
public interface InMemCompOpEvaluator extends CompOpEvaluator {

  /**
   * Evaluates the operator using the given in-memory objects.
   *
   * @param ctxt
   * @param compOp
   *          The compare operator to check.
   * @param attrValue
   *          The value found in the object to check.
   * @param compareToValue
   *          The restriction value to compare the object value(s) to.
   * @return <code>true</code> if the <code>attrValue</code> matches the
   *         <code>compareToValue</code> restriction.
   */
  boolean eval(InMemQueryEvaluator<?> ctxt, CompOp compOp, Object attrValue, Object compareToValue);

}
