package org.pm4j.common.query.inmem;

import java.util.List;

import org.pm4j.common.query.CompOpContains;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGe;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLe;
import org.pm4j.common.query.CompOpLike;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotContains;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpNotNull;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryEvaluatorSet;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprAnd;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryExprInMemCondition;
import org.pm4j.common.query.QueryExprNot;
import org.pm4j.common.query.QueryExprOr;

/**
 * The default set of in memory evaluators.
 *
 * @author olaf boede
 */
public class InMemQueryEvaluatorSet extends QueryEvaluatorSet {

  public static final QueryEvaluatorSet INSTANCE = new InMemQueryEvaluatorSet().lock();

  public InMemQueryEvaluatorSet() {
    addExprEvaluator(QueryExprAnd.class, new AndEvaluator());
    addExprEvaluator(QueryExprNot.class, new NotEvaluator());
    addExprEvaluator(QueryExprOr.class, new OrEvaluator());
    addExprEvaluator(QueryExprCompare.class, new CompareEvaluator());
    addExprEvaluator(QueryExprInMemCondition.class, new InMemConditionEvaluator());

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


  /** Evaluates logical AND expressions. */
  public static class AndEvaluator extends InMemExprEvaluatorBase<QueryExprAnd> {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprAnd expr) {
      List<QueryExpr> list = expr.getExpressions();
      if (list.isEmpty()) {
        throw new IllegalArgumentException("An AND expression should have at least a single member expression.");
      }
      for (QueryExpr e : list) {
        if (e == null) {
          throw new RuntimeException("An AND expression with a 'null' item can't be handled.");
        }

        InMemExprEvaluator ev = ctxt.getExprEvaluator(e);
        if (! ev.eval(ctxt, item, e)) {
          return false;
        }
      }
      // all conditions are true
      return true;
    }
  }

  /** Evaluates logical OR expressions. */
  public static class OrEvaluator extends InMemExprEvaluatorBase<QueryExprOr> {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprOr expr) {
      List<QueryExpr> list = expr.getExpressions();
      if (list.isEmpty()) {
        throw new IllegalArgumentException("An OR expression should have at least a single member expression.");
      }
      for (QueryExpr e : list) {
        InMemExprEvaluator ev = ctxt.getExprEvaluator(e);
        if (ev.eval(ctxt, item, e)) {
          return true;
        }
      }
      // all conditions are false
      return false;
    }
  }

  /** Evaluates logical compare expressions. */
  public static class CompareEvaluator extends InMemExprEvaluatorBase<QueryExprCompare> {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprCompare expr) {
      InMemCompOpEvaluator coEval = ctxt.getCompOpEvaluator(expr);
      QueryAttr attr = expr.getAttr();
      Object attrValue = ctxt.getAttrValue(item, attr);
      return coEval.eval(ctxt, expr.getCompOp(), attrValue, expr.getValue());
    }
  }

  /** Evaluates logical NOT expressions. */
  public static class NotEvaluator extends InMemExprEvaluatorBase<QueryExprNot> {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprNot expr) {
      QueryExpr baseExpr = expr.getBaseExpression();
      InMemExprEvaluator baseExprEv = ctxt.getExprEvaluator(baseExpr);
      return ! baseExprEv.eval(ctxt, item, baseExpr);
    }
  }

  /**
   * Evaluator for an only in-memory evaluatable query expression ({@link QueryExprInMemCondition}). */
  public static class InMemConditionEvaluator extends InMemExprEvaluatorBase<QueryExprInMemCondition<Object>> {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, QueryExprInMemCondition<Object> expr) {
      return expr.eval(item);
    }
  }

  /**
   * Base class for in memory expression evaluation.<br>
   * Provides a type safe implementation signature for the concrete implementation.
   *
   * @param <T_EXPR> the expression type to handle.
   */
  public static abstract class InMemExprEvaluatorBase<T_EXPR extends QueryExpr> implements InMemExprEvaluator {

    @Override
    @SuppressWarnings("unchecked")
    public boolean eval(InMemQueryEvaluator<?> ctxt, Object item, QueryExpr expr) {
      return evalImpl(ctxt, item, (T_EXPR)expr);
    }

    /**
     * Type safe signature for {@link InMemExprEvaluator#eval(InMemQueryEvaluator, Object, QueryExpr)}.
     */
    protected abstract boolean evalImpl(InMemQueryEvaluator<?> ctxt, Object item, T_EXPR expr);
  }

}
