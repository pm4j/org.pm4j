package org.pm4j.common.query.inmem;

import org.pm4j.common.query.FilterCompare;
import org.pm4j.common.query.QueryAttr;

/**
 * Evaluates logical compare expressions.
 *
 * @author olaf boede
 */
public class InMemExprEvaluatorCompare extends InMemExprEvaluatorBase<FilterCompare> {

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, FilterCompare expr) {
    InMemCompOpEvaluator coEval = ctxt.getCompOpEvaluator(expr);
    QueryAttr attr = expr.getAttr();
    Object attrValue = ctxt.getAttrValue(item, attr);
    return coEval.eval(ctxt, expr.getCompOp(), attrValue, expr.getValue());
  }

}
