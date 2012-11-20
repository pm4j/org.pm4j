package org.pm4j.common.query.inmem;

import org.pm4j.common.query.FilterExpression;

public abstract class InMemExprEvaluatorBase<T_EXPR extends FilterExpression, T_ITEM> implements InMemExprEvaluator<T_ITEM> {

  @SuppressWarnings("unchecked")
  @Override
  public boolean eval(InMemQueryEvaluator<T_ITEM> ctxt, T_ITEM item, FilterExpression expr) {
    return evalImpl(ctxt, item, (T_EXPR)expr);
  }

  protected abstract boolean evalImpl(InMemQueryEvaluator<T_ITEM> ctxt, T_ITEM item, T_EXPR expr);

}
