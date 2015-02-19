package org.pm4j.common.query.inmem;

import org.pm4j.common.query.CompOp;

/**
 * Base class for in-memory compare operation evaluators.
 *
 * @author olaf boede
 *
 * @param <T_COMP_OP> type of compare operator.
 * @param <T_VALUE> type of value to compare.
 */
public abstract class InMemCompOpEvaluatorBase<T_COMP_OP extends CompOp, T_VALUE> implements InMemCompOpEvaluator {

  /**
   * Translates the external call to a type specific internal
   * {@link #evalImpl(InMemQueryEvaluator, CompOp, Object, Object)} call..
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean eval(InMemQueryEvaluator<?> ctxt, CompOp compOp, Object attrValue, Object compareToValue) {
    return evalImpl(ctxt, (T_COMP_OP) compOp, (T_VALUE)attrValue, (T_VALUE)compareToValue);
  }

  /**
   * Type safe implementation evaluation method.
   *
   * @param ctxt
   *          Evaluation context information.
   * @param compOp
   *          The compare operation to execute.
   * @param attrValue
   *          The value found in the object to check.
   * @param compareToValue
   *          The restriction value to compare the object value(s) to.
   * @return <code>true</code> if the <code>attrValue</code> matches the
   *         <code>compareToValue</code> restriction.
   */
  protected abstract boolean evalImpl(InMemQueryEvaluator<?> ctxt, T_COMP_OP compOp, T_VALUE attrValue, T_VALUE compareToValue);

}
