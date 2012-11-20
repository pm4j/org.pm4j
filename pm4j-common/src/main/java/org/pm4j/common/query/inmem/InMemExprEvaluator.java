package org.pm4j.common.query.inmem;

import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterExpressionEvaluator;

public interface InMemExprEvaluator<T_ITEM> extends FilterExpressionEvaluator {

  boolean eval(InMemQueryEvaluator<T_ITEM> ctxt, T_ITEM item, FilterExpression expr);

}
