package org.pm4j.common.query.inmem;

import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterNot;

public class InMemExprEvaluatorNot extends InMemExprEvaluatorBase<FilterNot> {

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, FilterNot expr) {
    FilterExpression baseExpr = expr.getBaseExpression();
    InMemExprEvaluator baseExprEv = ctxt.getExprEvaluator(baseExpr);
    return ! baseExprEv.eval(ctxt, item, baseExpr);
  }

}
