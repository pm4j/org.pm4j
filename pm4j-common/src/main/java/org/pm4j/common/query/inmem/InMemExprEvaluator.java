package org.pm4j.common.query.inmem;

import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterExpressionEvaluator;

/**
 * Basic interface for in-memory filter expression evaluations.
 *
 * @author olaf boede
 *
 * TODO olaf: remove this item type
 * @param <T_ITEM> type of item to
 */
public interface InMemExprEvaluator extends FilterExpressionEvaluator {

  boolean eval(InMemQueryEvaluator<?> ctxt, Object item, FilterExpression expr);

}
