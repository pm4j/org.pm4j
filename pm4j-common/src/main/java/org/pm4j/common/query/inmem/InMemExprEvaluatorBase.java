package org.pm4j.common.query.inmem;

import org.pm4j.common.query.FilterExpression;

public abstract class InMemExprEvaluatorBase<T_EXPR extends FilterExpression> implements InMemExprEvaluator {

  @SuppressWarnings("unchecked")
  @Override
  public boolean eval(InMemQueryEvaluator<?> ctxt, Object item, FilterExpression expr) {
    return evalImpl(ctxt, item, (T_EXPR)expr);
  }

  protected abstract boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, T_EXPR expr);

}
