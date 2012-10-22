package org.pm4j.common.query.inmem;

import java.util.List;

import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterExpression;

public class InMemExprEvaluatorAnd extends InMemExprEvaluatorBase<FilterAnd, Object> {

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<Object> ctxt, Object item, FilterAnd expr) {
    List<FilterExpression> list = expr.getExpressions();
    if (list.isEmpty()) {
      throw new IllegalArgumentException("An AND expression should have at least a single member expression.");
    }
    for (FilterExpression e : list) {
      InMemExprEvaluator<Object> ev = ctxt.getExprEvaluator(e);
      if (! ev.eval(ctxt, item, e)) {
        return false;
      }
    }
    // all conditions are true
    return true;
  }


}
