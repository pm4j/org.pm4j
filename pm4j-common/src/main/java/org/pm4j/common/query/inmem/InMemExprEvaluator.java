package org.pm4j.common.query.inmem;

import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterExpressionEvaluator;

/**
 * Basic interface for in-memory filter expression evaluations.
 *
 * @author olaf boede
 */
public interface InMemExprEvaluator extends FilterExpressionEvaluator {

  /**
   * Evaluates a {@link FilterExpression} for a given item to <code>true</code>
   * or <code>false</code>.
   *
   * @param ctxt
   *          the query evaluator context. Used for call backs like
   *          {@link InMemQueryEvaluator#getAttrValue(Object, org.pm4j.common.query.QueryAttr)}.
   * @param item
   *          the data object to check the filter expression for.
   * @param expr
   *          the filter expression to apply.
   * @return <code>true</code> if the given item did match the filter criteria.
   */
  boolean eval(InMemQueryEvaluator<?> ctxt, Object item, FilterExpression expr);

}
