package org.pm4j.common.query.inmem;

import org.pm4j.common.query.QueryExpr;

/**
 * Base class for in memory expression evaluation.<br>
 * Provides a type safe implementation signature for the concrete implementation.
 *
 * @author olaf boede
 *
 * @param <T_EXPR> the expression type to handle.
 */
public abstract class InMemExprEvaluatorBase<T_EXPR extends QueryExpr> implements InMemExprEvaluator {

  @Override
  @SuppressWarnings("unchecked")
  public boolean eval(InMemQueryEvaluator<?> ctxt, Object item, QueryExpr expr) {
    return evalImpl(ctxt, item, (T_EXPR)expr);
  }

  /**
   * Type safe signature for {@link InMemExprEvaluator#eval(InMemQueryEvaluator, Object, QueryExpr)}.
   */
  protected abstract boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, T_EXPR expr);

}
