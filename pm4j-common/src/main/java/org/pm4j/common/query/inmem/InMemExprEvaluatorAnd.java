package org.pm4j.common.query.inmem;

import java.util.List;

import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterExpression;

/**
 * Evaluates logical AND expressions.
 *
 * @author olaf boede
 */
public class InMemExprEvaluatorAnd extends InMemExprEvaluatorBase<FilterAnd> {

  public static final InMemExprEvaluatorAnd INSTANCE = new InMemExprEvaluatorAnd();

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, FilterAnd expr) {
    List<FilterExpression> list = expr.getExpressions();
    if (list.isEmpty()) {
      throw new IllegalArgumentException("An AND expression should have at least a single member expression.");
    }
    for (FilterExpression e : list) {
      if (e == null) {
        throw new RuntimeException("An AND expression with a 'null' item can't be handled.");
      }

      InMemExprEvaluator ev = ctxt.getExprEvaluator(e);
      if (! ev.eval(ctxt, item, e)) {
        return false;
      }
    }
    // all conditions are true
    return true;
  }


}
