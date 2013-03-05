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
   * External operation execution interface method.
   *
   * @param ctxt evaluation context information.
   * @param compOp the compare operation to execute.
   * @param lhs left hand side value to compare.
   * @param rhs right hand side value to compare.
   * @return <code>true</code> if the expression evaluates to <code>true</code>.
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean eval(InMemQueryEvaluator<?> ctxt, CompOp compOp, Object lhs, Object rhs) {
    return evalImpl(ctxt, (T_COMP_OP) compOp, (T_VALUE)lhs, (T_VALUE)rhs);
  }

  /**
   * Type safe implementation evaluation method.
   *
   * @param ctxt evaluation context information.
   * @param compOp the compare operation to execute.
   * @param lhs left hand side value to compare.
   * @param rhs right hand side value to compare.
   * @return <code>true</code> if the expression evaluates to <code>true</code>.
   */
  protected abstract boolean evalImpl(InMemQueryEvaluator<?> ctxt, T_COMP_OP compOp, T_VALUE lhs, T_VALUE rhs);

}
