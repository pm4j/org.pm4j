package org.pm4j.common.query;


/**
 * Base class for technology (e.g. in-memory, db based) query evaluator instances.
 * <p>
 * It holds a set of technology specific evaluators for the set of supported compare operations.
 *
 * @author olaf boede
 */
// TODO olaf: Check if its mini-functionality can't be moved to the EvaluatorSet. -> reduce number of artifacts!
// Remove from in-memory implementation too.
@Deprecated
public class QueryEvaluatorBase {

  private QueryEvaluatorSet evaluatorSet;

  public QueryEvaluatorBase() {
    this(new QueryEvaluatorSet());
  }

  public QueryEvaluatorBase(QueryEvaluatorSet evaluatorSet) {
    this.evaluatorSet = evaluatorSet;
  }

  protected Object getExprEvaluator(FilterExpression expr) {
    return evaluatorSet.getExprEvaluator(expr);
  }

  protected Object getCompOpEvaluator(FilterCompare filterCompare) {
    return evaluatorSet.getCompOpEvaluator(filterCompare);
  }

  protected void setEvaluatorSet(QueryEvaluatorSet evaluatorSet) {
    this.evaluatorSet = evaluatorSet;
  }

  public QueryEvaluatorSet getEvaluatorSet() {
    return evaluatorSet;
  }

}
