package org.pm4j.common.query;


/**
 * Base class for technology (e.g. in-memory, db based) query evaluator instances.
 * <p>
 * It holds a set of technology specific evaluators for the set of supported compare operations.
 *
 * @author olaf boede
 */
// TODO olaf: Check if its mini-functionality can't be moved to the EvaluatorSet. -> reduce number of artifacts!
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
