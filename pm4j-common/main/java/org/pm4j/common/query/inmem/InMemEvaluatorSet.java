package org.pm4j.common.query.inmem;

import java.util.Map;

import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpStringIsEmpty;
import org.pm4j.common.query.CompOpStringNotIsEmpty;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.query.EvaluatorSet;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterCompare;
import org.pm4j.common.query.FilterNot;
import org.pm4j.common.query.FilterOr;
import org.pm4j.common.util.collection.MapUtil;

/**
 * The default set of in memory evaluators.
 *
 * @author olaf boede
 */
public class InMemEvaluatorSet extends EvaluatorSet {

  public static EvaluatorSet INSTANCE = new InMemEvaluatorSet();

  public InMemEvaluatorSet() {
    super(
        makeClassToEvaluatorMap(
            FilterAnd.class, new InMemExprEvaluatorAnd(),
            FilterNot.class, new InMemExprEvaluatorNot(),
            FilterOr.class, new InMemExprEvaluatorOr(),
            FilterCompare.class, new InMemExprEvaluatorCompare()
        ),
        makeClassToEvaluatorMap(
            CompOpEquals.class, InMemCompOpEvaluators.EQUALS,
            CompOpGt.class, InMemCompOpEvaluators.GT,
            CompOpIsNull.class, InMemCompOpEvaluators.IS_NULL,
            CompOpLt.class, InMemCompOpEvaluators.LT,
            CompOpNotEquals.class, InMemCompOpEvaluators.NE,
            CompOpStringIsEmpty.class, InMemCompOpEvaluators.STRING_IS_EMPTY,
            CompOpStringNotIsEmpty.class, InMemCompOpEvaluators.STRING_IS_NOT_EMPTY,
            CompOpStringStartsWith.class, InMemCompOpEvaluators.STRING_STARTS_WITH
        ));
  }

  @SuppressWarnings("unchecked")
  private static Map<Class<?>, Object> makeClassToEvaluatorMap(Object... objects) {
    return (Map<Class<?>, Object>)(Object)MapUtil.makeFixHashMap(objects);
  }
}
