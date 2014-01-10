package org.pm4j.common.query.inmem;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGe;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLe;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpNotNull;
import org.pm4j.common.query.CompOpContains;
import org.pm4j.common.query.CompOpNotContains;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.util.CompareUtil;

// TODO olaf: move to InMemEvaluatorSet to reduce the number of artifacts.
public class InMemCompOpEvaluators {

  public static final InMemCompOpEvaluator EQUALS = new InMemCompOpEvaluatorBase<CompOpEquals, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpEquals compOp, Object attrValue, Object compareToValue) {
      return ObjectUtils.equals(attrValue, compareToValue);
    }
  };

  public static final InMemCompOpEvaluator GE = new InMemCompOpEvaluatorBase<CompOpGe, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpGe compOp, Comparable<?> attrValue, Comparable<?> compareToValue) {
      return CompareUtil.compare(attrValue, compareToValue) >= 0;
    }
  };

  public static final InMemCompOpEvaluator GT = new InMemCompOpEvaluatorBase<CompOpGt, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpGt compOp, Comparable<?> attrValue, Comparable<?> compareToValue) {
      return CompareUtil.compare(attrValue, compareToValue) > 0;
    }
  };

  public static final InMemCompOpEvaluator IS_NULL = new InMemCompOpEvaluatorBase<CompOpIsNull, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpIsNull compOp, Object attrValue, Object compareToValue) {
      return attrValue == null || (attrValue instanceof String && StringUtils.isEmpty((String)attrValue));
    }
  };

  public static final InMemCompOpEvaluator LE = new InMemCompOpEvaluatorBase<CompOpLe, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpLe compOp, Comparable<?> attrValue, Comparable<?> compareToValue) {
      return CompareUtil.compare(attrValue, compareToValue) <= 0;
    }
  };

  public static final InMemCompOpEvaluator LIKE = new InMemCompOpEvaluatorLike();

  public static final InMemCompOpEvaluator LT = new InMemCompOpEvaluatorBase<CompOpLt, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpLt compOp, Comparable<?> attrValue, Comparable<?> compareToValue) {
      return CompareUtil.compare(attrValue, compareToValue) < 0;
    }
  };

  public static final InMemCompOpEvaluator NE = new InMemCompOpEvaluatorBase<CompOpNotEquals, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpNotEquals compOp, Object attrValue, Object compareToValue) {
      return !ObjectUtils.equals(attrValue, compareToValue);
    }
  };

  public static final InMemCompOpEvaluator NOT_NULL = new InMemCompOpEvaluatorBase<CompOpNotNull, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpNotNull compOp, Object attrValue, Object compareToValue) {
      return (attrValue instanceof String)
          ? StringUtils.isNotEmpty((String)attrValue)
          : attrValue != null;
    }
  };

  public static final InMemCompOpEvaluator STARTS_WITH = new InMemCompOpEvaluatorBase<CompOpStartsWith, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpStartsWith compOp, String attrValue, String compareToValue) {
      return CompareUtil.indexOf(attrValue, compareToValue, compOp.isIgnoreCase(), compOp.isIgnoreSpaces()) == 0;
    }
  };

  public static final InMemCompOpEvaluator CONTAINS = new InMemCompOpEvaluatorBase<CompOpContains, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpContains compOp, String attrValue, String compareToValue) {
      return CompareUtil.indexOf(attrValue, compareToValue, compOp.isIgnoreCase(), compOp.isIgnoreSpaces()) != -1;
    }
  };

  public static final InMemCompOpEvaluator NOT_CONTAINS = new InMemCompOpEvaluatorBase<CompOpNotContains, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpNotContains compOp, String attrValue, String compareToValue) {
      return CompareUtil.indexOf(attrValue, compareToValue, compOp.isIgnoreCase(), compOp.isIgnoreSpaces()) == -1;
    }
  };

  public static final InMemCompOpEvaluator IN = new InMemCompOpEvaluatorBase<CompOpIn, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpIn compOp, Object attrValue, Object compareToValue) {
      if (! (compareToValue instanceof Collection)) {
        throw new IllegalArgumentException("The IN operator expects a Collection as parameter.");
      }
      return ((Collection<?>)compareToValue).contains(attrValue);
    }
  };


}
