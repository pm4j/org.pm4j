package org.pm4j.common.query;


public class QueryEvaluatorBase {

  private EvaluatorSet evaluatorSet;

  public QueryEvaluatorBase() {
    this(new EvaluatorSet());
  }

  public QueryEvaluatorBase(EvaluatorSet evaluatorSet) {
    this.evaluatorSet = evaluatorSet;
  }

  protected Object getExprEvaluator(FilterExpression expr) {
    return evaluatorSet.getExprEvaluator(expr);
  }

  protected Object getCompOpEvaluator(CompOp compOp) {
    return evaluatorSet.getCompOpEvaluator(compOp);
  }

  protected void setEvaluatorSet(EvaluatorSet evaluatorSet) {
    this.evaluatorSet = evaluatorSet;
  }

}
