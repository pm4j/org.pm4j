package org.pm4j.common.query;

import java.util.HashMap;
import java.util.Map;

/**
 * A (technology specific) set of {@link FilterExpressionEvaluator}s and {@link CompOp} evaluators.
 *
 * @author olaf boede
 */
public class QueryEvaluatorSet {

  /** The set of {@link FilterExpression} classes mapped to corresponding evaluator instances. */
  private final Map<Class<?>, Object> exprEvaluatorMap = new HashMap<Class<?>, Object>();

  /** The set of {@link CompOp} classes mapped to corresponding evaluator instances. */
  private final Map<Class<?>, CompOpEvaluator> defaultAttrCompOpEvaluatorMap = new HashMap<Class<?>, CompOpEvaluator>();

  /** The {@link QueryAttr} type specific sets of {@link CompOp} classes mapped to corresponding evaluator instances. */
  private final Map<Class<?>, Map<Class<?>, CompOpEvaluator>> attrTypeToCompOpEvaluatorMap = new HashMap<Class<?>, Map<Class<?>, CompOpEvaluator>>();

  /** {@link QueryAttr} specific {@link CompOp} maps.  */
  private final Map<QueryAttr, Map<Class<?>, CompOpEvaluator>> attrToCompOpEvaluatorMap = new HashMap<QueryAttr, Map<Class<?>, CompOpEvaluator>>();

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
  public CompOpEvaluator getCompOpEvaluator(FilterCompare filterCompare) {
    if (filterCompare.getCompOp() == null) {
      throw new RuntimeException("The filter parameter has no CompOp instance. " + filterCompare);
    }

    Class<?> attrClass = filterCompare.getAttr().getClass();
    Class<?> compOpClass = filterCompare.getCompOp().getClass();

    for (Map.Entry<Class<?>, Map<Class<?>, CompOpEvaluator>> e : attrTypeToCompOpEvaluatorMap.entrySet()) {
      if (e.getKey().isAssignableFrom(attrClass)) {
        CompOpEvaluator ev = e.getValue().get(compOpClass);
        if (ev != null) {
          return ev;
        }
      }
    }

    CompOpEvaluator ev = defaultAttrCompOpEvaluatorMap.get(compOpClass);
    if (ev == null) {
      throw new RuntimeException("Missing compare operator evaluator for operator type: '" + filterCompare.getClass() +
          "' evaluator set: " + this);
    }
    return ev;
  }

  /**
   * Adds an evaluator for a particular filter operator. If the operator is already registered
   * with an evaluator, it will be replaced with the new evaluator passed.
   *
   * @param filterExpressionClass
   *            the type of filter expression
   * @param evaluator
   *            the evaluator to use to interpret the expression instances
   */
  public void addExprEvaluator(Class<? extends FilterExpression> exprClass, FilterExpressionEvaluator e) {
    exprEvaluatorMap.put(exprClass, e);
  }

  /**
   * Adds an evaluator for a particular compare operator. If the operator is already
   * registered with an evaluator, it will be replaced with the new evaluator passed.
   *
   * @param compOpClass
   *            compare operator
   * @param evaluator
   *            compare operator evaluator (interpreter)
   */
  public void addCompOpEvaluator(Class<? extends CompOp> compOpClass, CompOpEvaluator e) {
    addCompOpEvaluator(AttrDefinition.class, compOpClass, e);
  }

  /**
   * Adds an attribute kind specific evaluator for a particular compare
   * operator. If the operator is already registered with an evaluator, it will
   * be replaced with the new evaluator passed.
   *
   * @param forAttrClass
   *          the kind of {@link QueryAttr} this evaluator may be applied for.
   * @param compOpClass
   *          compare operator
   * @param evaluator
   *          compare operator evaluator (interpreter)
   */
  public void addCompOpEvaluator(Class<? extends QueryAttr> forAttrClass, Class<? extends CompOp> compOpClass, CompOpEvaluator e) {
    if (forAttrClass == AttrDefinition.class) {
      defaultAttrCompOpEvaluatorMap.put(compOpClass, e);
      return;
    }

    Map<Class<?>, CompOpEvaluator> map = attrTypeToCompOpEvaluatorMap.get(forAttrClass);
    if (map == null) {
      map = new HashMap<Class<?>, CompOpEvaluator>();
      attrTypeToCompOpEvaluatorMap.put(forAttrClass, map);
    }
    map.put(compOpClass, e);
  }

  /**
   * Adds an attribute specific evaluator for a particular compare
   * operator. If the operator is already registered with an evaluator, it will
   * be replaced with the new evaluator passed.
   *
   * @param forAttr
   *          the {@link QueryAttr} this evaluator may be applied for.
   * @param compOpClass
   *          compare operator
   * @param evaluator
   *          compare operator evaluator (interpreter)
   */
  public void addCompOpEvaluator(QueryAttr forAttr, Class<? extends CompOp> compOpClass, CompOpEvaluator e) {
    Map<Class<?>, CompOpEvaluator> map = attrToCompOpEvaluatorMap.get(forAttr);
    if (map == null) {
      map = new HashMap<Class<?>, CompOpEvaluator>();
      attrToCompOpEvaluatorMap.put(forAttr, map);
    }
    map.put(compOpClass, e);
  }

}
