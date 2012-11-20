package org.pm4j.common.query;

import java.util.HashMap;
import java.util.Map;

public class EvaluatorSet {

  private final Map<Class<?>, Object> exprEvaluatorMap;
  private final Map<Class<?>, Object> compOpEvaluatorMap;

  public EvaluatorSet() {
    this(new HashMap<Class<?>, Object>(), new HashMap<Class<?>, Object>());
  }

  public EvaluatorSet(Map<Class<?>, Object> exprEvaluatorMap, Map<Class<?>, Object> compOpEvaluatorMap) {
    assert exprEvaluatorMap != null;
    assert compOpEvaluatorMap != null;

    this.exprEvaluatorMap = exprEvaluatorMap;
    this.compOpEvaluatorMap = compOpEvaluatorMap;
  }

  /**
   * Provides the matching evaluator for the given expression.
   *
   * @param expr the expression to evaluate.
   * @return a technology specific instance that can evaluate the expression.
   */
  public Object getExprEvaluator(FilterExpression expr) {
    Object ev = exprEvaluatorMap.get(expr.getClass());
    if (ev == null) {
      throw new RuntimeException("Missing filter expression evaluator for expression type: " + expr.getClass());
    }
    return ev;
  }

  /**
   * Provides the matching compare operator for the given expression.
   *
   * @param expr the compare operator to evaluate.
   * @return a technology specific instance that can evaluate the compare operation.
   */
  public Object getCompOpEvaluator(CompOp compOp) {
    Object ev = compOpEvaluatorMap.get(compOp.getClass());
    if (ev == null) {
      throw new RuntimeException("Missing compare operator evaluator for operator type: '" + compOp.getClass() +
          "' evaluator set: " + this);
    }
    return ev;
  }

  public void addExprEvaluator(Class<? extends FilterExpression> exprClass, FilterExpressionEvaluator e) {
    exprEvaluatorMap.put(exprClass, e);
  }

  public void addCompOpEvaluator(Class<? extends CompOp> compOpClass, CompOpEvaluator e) {
    compOpEvaluatorMap.put(compOpClass, e);
  }
}
