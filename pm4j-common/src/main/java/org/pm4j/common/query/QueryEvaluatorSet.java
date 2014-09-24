package org.pm4j.common.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A (technology specific) set of {@link QueryExprEvaluator}s and {@link CompOp} evaluators.
 *
 * @author olaf boede
 */
public class QueryEvaluatorSet {

  /**
   * The set of {@link QueryExpr} classes mapped to corresponding evaluator instances.
   * <p>
   * Is a concurrent map because it gets extended at evaluation time with missing subclass mappings.
   * That may happen concurrently.
   */
  private final Map<Class<?>, Object> exprEvaluatorMap = new ConcurrentHashMap<Class<?>, Object>();

  /** The set of {@link CompOp} classes mapped to corresponding evaluator instances. */
  private final Map<Class<?>, CompOpEvaluator> defaultAttrCompOpEvaluatorMap = new HashMap<Class<?>, CompOpEvaluator>();

  /** The {@link QueryAttr} type specific sets of {@link CompOp} classes mapped to corresponding evaluator instances. */
  private final Map<Class<?>, Map<Class<?>, CompOpEvaluator>> attrClassToCompOpEvaluatorMap = new HashMap<Class<?>, Map<Class<?>, CompOpEvaluator>>();
  
  /** The {@link QueryAttr} value type specific sets of {@link CompOp} classes mapped to corresponding evaluator instances. */
  private final Map<Class<?>, Map<Class<?>, CompOpEvaluator>> valueTypeToCompOpEvaluatorMap = new HashMap<Class<?>, Map<Class<?>, CompOpEvaluator>>();

  /** {@link QueryAttr} specific {@link CompOp} maps.  */
  private final Map<QueryAttr, Map<Class<?>, CompOpEvaluator>> attrToCompOpEvaluatorMap = new HashMap<QueryAttr, Map<Class<?>, CompOpEvaluator>>();

  /** A lock that may be activated for shared instances to prevent unwanted side effects from calling an add-method. */
  private boolean locked;

  /**
   * Provides the matching evaluator for the given expression.
   *
   * @param expr the expression to evaluate.
   * @return a technology specific instance that can evaluate the expression.
   */
  public Object getExprEvaluator(QueryExpr expr) {
    Class<?> exprClass = expr.getClass();
    Object ev = findExprEvaluator(exprClass);
    if (ev == null) {
      throw new RuntimeException("Missing filter expression evaluator for expression type: " + exprClass);
    }
    return ev;
  }

  /**
   * Provides the matching compare operator for the given expression.
   *
   * @param expr the compare operator to evaluate.
   * @return a technology specific instance that can evaluate the compare operation.
   */
  public CompOpEvaluator getCompOpEvaluator(QueryExprCompare filterCompare) {
    if (filterCompare.getCompOp() == null) {
      throw new RuntimeException("The filter parameter has no CompOp instance. " + filterCompare);
    }
    
    QueryAttr attr = filterCompare.getAttr();
    Class<?> compOpClass = filterCompare.getCompOp().getClass();

    // TODO MHE: Does not work for subclasses. But for final types (e.g LocalDateTime) is nothing to do.
    if (valueTypeToCompOpEvaluatorMap.containsKey(attr.getType())) {
      Map<Class<?>, CompOpEvaluator> map = valueTypeToCompOpEvaluatorMap.get(attr.getType());
      
      if (map.containsKey(compOpClass)) {
        return map.get(compOpClass);
      }
    }

    Class<?> attrClass = filterCompare.getAttr().getClass();

    for (Map.Entry<Class<?>, Map<Class<?>, CompOpEvaluator>> e : attrClassToCompOpEvaluatorMap.entrySet()) {
      if (e.getKey().isAssignableFrom(attrClass)) {
        CompOpEvaluator ev = e.getValue().get(compOpClass);
        if (ev != null) {
          return ev;
        }
      }
    }

    CompOpEvaluator ev = defaultAttrCompOpEvaluatorMap.get(compOpClass);
    if (ev == null) {
      throw new RuntimeException("Missing compare operator evaluator for operator type: '" + compOpClass +
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
  public void addExprEvaluator(Class<? extends QueryExpr> exprClass, QueryExprEvaluator e) {
    assertUnlocked();
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
    addCompOpEvaluator(QueryAttr.class, compOpClass, e);
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
    assertUnlocked();
    if (forAttrClass == QueryAttr.class) {
      defaultAttrCompOpEvaluatorMap.put(compOpClass, e);
      return;
    }

    Map<Class<?>, CompOpEvaluator> map = attrClassToCompOpEvaluatorMap.get(forAttrClass);
    if (map == null) {
      map = new HashMap<Class<?>, CompOpEvaluator>();
      attrClassToCompOpEvaluatorMap.put(forAttrClass, map);
    }
    map.put(compOpClass, e);
  }
  
  /**
   * Adds a valueType specific evaluator for a particular compare
   * operator. If the operator is already registered with an evaluator, it will
   * be replaced with the new evaluator passed.
   * 
   * ATTENTION: Does not yet work for subclasses of the provided type!
   *
   * @param forValueType
   *          the value type provided by {@link QueryAttr#getType()} this evaluator may be applied for.
   * @param compOpClass
   *          compare operator
   * @param evaluator
   *          compare operator evaluator (interpreter)
   */
  public void addCompOpEvaluatorForValueType(Class<?> forValueType, Class<? extends CompOp> compOpClass, CompOpEvaluator e) {
    assertUnlocked();
    
    if (!valueTypeToCompOpEvaluatorMap.containsKey(forValueType)) {
      valueTypeToCompOpEvaluatorMap.put(forValueType, new HashMap<Class<?>, CompOpEvaluator>());
    }
    
    Map<Class<?>, CompOpEvaluator> map = valueTypeToCompOpEvaluatorMap.get(forValueType);
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
    assertUnlocked();
    Map<Class<?>, CompOpEvaluator> map = attrToCompOpEvaluatorMap.get(forAttr);
    if (map == null) {
      map = new HashMap<Class<?>, CompOpEvaluator>();
      attrToCompOpEvaluatorMap.put(forAttr, map);
    }
    map.put(compOpClass, e);
  }

  /**
   * Locks this instance. A further call to an add-method will throw an exception.
   */
  @SuppressWarnings("unchecked")
  public <T extends QueryEvaluatorSet> T lock() {
    locked = true;
    return (T) this;
  }

  /**
   * Throws an exception if the instance is locked.
   */
  protected void assertUnlocked() {
    if (locked) {
      throw new RuntimeException("This query evaluator set is a shared locked instance. Please create another evaluator set instance if you need a specific one.");
    }
  }

  /**
   * Finds the matching evaluator.<p>
   * In case of a sub-class that is not directly mapped to an evaluator, the map will be extended to
   * automatically to improve match speed for the next find operation.
   *
   * @param exprClass The expression class or interface to find an evaluator for.
   * @return The found evaluator or <code>null</code>.
   */
  private Object findExprEvaluator(Class<?> exprClass) {
    Object ev = exprEvaluatorMap.get(exprClass);
    if (ev != null) {
      return ev;
    }

    if (exprClass.getSuperclass() != Object.class) {
      ev = findExprEvaluator(exprClass.getSuperclass());
      if (ev != null) {
        exprEvaluatorMap.put(exprClass, ev);
        return ev;
      }
    }

    for (Class<?> c : exprClass.getInterfaces()) {
      ev = findExprEvaluator(c);
      if (ev != null) {
        exprEvaluatorMap.put(exprClass, ev);
        return ev;
      }
    }

    // not found
    return null;
  }

}
