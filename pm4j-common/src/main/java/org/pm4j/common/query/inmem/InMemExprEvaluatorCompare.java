package org.pm4j.common.query.inmem;

import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.FilterCompare;

public class InMemExprEvaluatorCompare extends InMemExprEvaluatorBase<FilterCompare> {

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, FilterCompare expr) {
    CompOp compOp = expr.getCompOp();
    InMemCompOpEvaluator coEval = ctxt.getCompOpEvaluator(compOp);
    AttrDefinition attr = expr.getAttr();
    Object attrValue = ctxt.getAttrValue(item, attr);
    return coEval.eval(ctxt, compOp, attrValue, expr.getValue());
  }

}
