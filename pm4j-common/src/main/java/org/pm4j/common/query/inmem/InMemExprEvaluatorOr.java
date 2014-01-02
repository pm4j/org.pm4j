package org.pm4j.common.query.inmem;

import java.util.List;

import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprOr;

/**
 * Evaluates logical OR expressions.
 *
 * @author olaf boede
 */
public class InMemExprEvaluatorOr extends InMemExprEvaluatorBase<QueryExprOr> {

  public static final InMemExprEvaluatorOr INSTANCE = new InMemExprEvaluatorOr();

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprOr expr) {
    List<QueryExpr> list = expr.getExpressions();
    if (list.isEmpty()) {
      throw new IllegalArgumentException("An OR expression should have at least a single member expression.");
    }
    for (QueryExpr e : list) {
      InMemExprEvaluator ev = ctxt.getExprEvaluator(e);
      if (ev.eval(ctxt, item, e)) {
        return true;
      }
    }
    // all conditions are false
    return false;
  }

}
