package org.pm4j.common.query.inmem;

import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprNot;

/**
 * Evaluates logical NOT expressions.
 *
 * @author olaf boede
 */
public class InMemExprEvaluatorNot extends InMemExprEvaluatorBase<QueryExprNot> {

  public static final InMemExprEvaluatorNot INSTANCE = new InMemExprEvaluatorNot();

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprNot expr) {
    QueryExpr baseExpr = expr.getBaseExpression();
    InMemExprEvaluator baseExprEv = ctxt.getExprEvaluator(baseExpr);
    return ! baseExprEv.eval(ctxt, item, baseExpr);
  }

}
