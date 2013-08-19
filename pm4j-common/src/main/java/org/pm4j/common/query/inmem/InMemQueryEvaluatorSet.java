package org.pm4j.common.query.inmem;

import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpStringContains;
import org.pm4j.common.query.CompOpStringNotContains;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterCompare;
import org.pm4j.common.query.FilterNot;
import org.pm4j.common.query.FilterOr;
import org.pm4j.common.query.QueryEvaluatorSet;

/**
 * The default set of in memory evaluators.
 *
 * @author olaf boede
 */
public class InMemQueryEvaluatorSet extends QueryEvaluatorSet {

  public static QueryEvaluatorSet INSTANCE = new InMemQueryEvaluatorSet();

  public InMemQueryEvaluatorSet() {
    addExprEvaluator(FilterAnd.class, new InMemExprEvaluatorAnd());
    addExprEvaluator(FilterNot.class, new InMemExprEvaluatorNot());
    addExprEvaluator(FilterOr.class, new InMemExprEvaluatorOr());
    addExprEvaluator(FilterCompare.class, new InMemExprEvaluatorCompare());

    addCompOpEvaluator(CompOpEquals.class, InMemCompOpEvaluators.EQUALS);
    addCompOpEvaluator(CompOpGt.class, InMemCompOpEvaluators.GT);
    addCompOpEvaluator(CompOpIsNull.class, InMemCompOpEvaluators.IS_NULL);
    addCompOpEvaluator(CompOpLt.class, InMemCompOpEvaluators.LT);
    addCompOpEvaluator(CompOpNotEquals.class, InMemCompOpEvaluators.NE);
    addCompOpEvaluator(CompOpStringStartsWith.class, InMemCompOpEvaluators.STRING_STARTS_WITH);
    addCompOpEvaluator(CompOpStringContains.class, InMemCompOpEvaluators.STRING_CONTAINS);
    addCompOpEvaluator(CompOpStringNotContains.class, InMemCompOpEvaluators.STRING_NOT_CONTAINS);
    addCompOpEvaluator(CompOpIn.class, InMemCompOpEvaluators.IN);
  }

}
