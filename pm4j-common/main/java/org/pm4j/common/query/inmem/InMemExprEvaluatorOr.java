package org.pm4j.common.query.inmem;

import java.util.List;

import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterOr;

public class InMemExprEvaluatorOr extends InMemExprEvaluatorBase<FilterOr, Object> {

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<Object> ctxt, Object item, FilterOr expr) {
    List<FilterExpression> list = expr.getExpressions();
    if (list.isEmpty()) {
      throw new IllegalArgumentException("An OR expression should have at least a single member expression.");
    }
    for (FilterExpression e : list) {
      InMemExprEvaluator<Object> ev = ctxt.getExprEvaluator(e);
      if (ev.eval(ctxt, item, e)) {
        return true;
      }
    }
    // all conditions are false
    return false;
  }

}
