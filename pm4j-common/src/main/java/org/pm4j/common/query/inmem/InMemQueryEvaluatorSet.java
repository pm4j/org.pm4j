package org.pm4j.common.query.inmem;

import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGe;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLe;
import org.pm4j.common.query.CompOpLike;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpNotNull;
import org.pm4j.common.query.CompOpContains;
import org.pm4j.common.query.CompOpNotContains;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryExprAnd;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryExprNot;
import org.pm4j.common.query.QueryExprOr;
import org.pm4j.common.query.QueryEvaluatorSet;

/**
 * The default set of in memory evaluators.
 *
 * @author olaf boede
 */
public class InMemQueryEvaluatorSet extends QueryEvaluatorSet {

  public static QueryEvaluatorSet INSTANCE = new InMemQueryEvaluatorSet();

  public InMemQueryEvaluatorSet() {
    addExprEvaluator(QueryExprAnd.class, new InMemExprEvaluatorAnd());
    addExprEvaluator(QueryExprNot.class, new InMemExprEvaluatorNot());
    addExprEvaluator(QueryExprOr.class, new InMemExprEvaluatorOr());
    addExprEvaluator(QueryExprCompare.class, new InMemExprEvaluatorCompare());

    addCompOpEvaluator(CompOpEquals.class, InMemCompOpEvaluators.EQUALS);
    addCompOpEvaluator(CompOpGe.class, InMemCompOpEvaluators.GE);
    addCompOpEvaluator(CompOpGt.class, InMemCompOpEvaluators.GT);
    addCompOpEvaluator(CompOpIsNull.class, InMemCompOpEvaluators.IS_NULL);
    addCompOpEvaluator(CompOpLe.class, InMemCompOpEvaluators.LE);
    addCompOpEvaluator(CompOpLike.class, InMemCompOpEvaluators.LIKE);
    addCompOpEvaluator(CompOpLt.class, InMemCompOpEvaluators.LT);
    addCompOpEvaluator(CompOpNotEquals.class, InMemCompOpEvaluators.NE);
    addCompOpEvaluator(CompOpNotNull.class, InMemCompOpEvaluators.NOT_NULL);
    addCompOpEvaluator(CompOpStartsWith.class, InMemCompOpEvaluators.STARTS_WITH);
    addCompOpEvaluator(CompOpContains.class, InMemCompOpEvaluators.CONTAINS);
    addCompOpEvaluator(CompOpNotContains.class, InMemCompOpEvaluators.NOT_CONTAINS);
    addCompOpEvaluator(CompOpIn.class, InMemCompOpEvaluators.IN);
  }

}
