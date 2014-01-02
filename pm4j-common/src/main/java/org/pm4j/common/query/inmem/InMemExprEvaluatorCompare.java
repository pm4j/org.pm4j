package org.pm4j.common.query.inmem;

import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryAttr;

/**
 * Evaluates logical compare expressions.
 *
 * @author olaf boede
 */
public class InMemExprEvaluatorCompare extends InMemExprEvaluatorBase<QueryExprCompare> {

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprCompare expr) {
    InMemCompOpEvaluator coEval = ctxt.getCompOpEvaluator(expr);
    QueryAttr attr = expr.getAttr();
    Object attrValue = ctxt.getAttrValue(item, attr);
    return coEval.eval(ctxt, expr.getCompOp(), attrValue, expr.getValue());
  }

}
