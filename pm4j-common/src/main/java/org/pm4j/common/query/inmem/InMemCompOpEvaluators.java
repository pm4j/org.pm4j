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
import org.pm4j.common.query.CompOpStringContains;
import org.pm4j.common.query.CompOpStringNotContains;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.util.CompareUtil;

// TODO olaf: move to InMemEvaluatorSet to reduce the number of artifacts.
public class InMemCompOpEvaluators {

  public static final InMemCompOpEvaluator EQUALS = new InMemCompOpEvaluatorBase<CompOpEquals, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpEquals compOp, Object o1, Object o2) {
      return ObjectUtils.equals(o1, o2);
    }
  };

  public static final InMemCompOpEvaluator GE = new InMemCompOpEvaluatorBase<CompOpGe, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpGe compOp, Comparable<?> o1, Comparable<?> o2) {
      return CompareUtil.compare(o1, o2) >= 0;
    }
  };

  public static final InMemCompOpEvaluator GT = new InMemCompOpEvaluatorBase<CompOpGt, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpGt compOp, Comparable<?> o1, Comparable<?> o2) {
      return CompareUtil.compare(o1, o2) > 0;
    }
  };

  public static final InMemCompOpEvaluator IS_NULL = new InMemCompOpEvaluatorBase<CompOpIsNull, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpIsNull compOp, Object o1, Object o2) {
      return o1 == null || (o1 instanceof String && StringUtils.isEmpty((String)o1));
    }
  };

  public static final InMemCompOpEvaluator LE = new InMemCompOpEvaluatorBase<CompOpLe, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpLe compOp, Comparable<?> o1, Comparable<?> o2) {
      return CompareUtil.compare(o1, o2) <= 0;
    }
  };

  public static final InMemCompOpEvaluator LT = new InMemCompOpEvaluatorBase<CompOpLt, Comparable<?>>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpLt compOp, Comparable<?> o1, Comparable<?> o2) {
      return CompareUtil.compare(o1, o2) < 0;
    }
  };

  public static final InMemCompOpEvaluator NE = new InMemCompOpEvaluatorBase<CompOpNotEquals, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpNotEquals compOp, Object o1, Object o2) {
      return !ObjectUtils.equals(o1, o2);
    }
  };

  public static final InMemCompOpEvaluator STRING_STARTS_WITH = new InMemCompOpEvaluatorBase<CompOpStringStartsWith, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpStringStartsWith compOp, String o1, String o2) {
      return CompareUtil.indexOf(o1, o2, compOp.isIgnoreCase(), compOp.isIgnoreSpaces()) == 0;
    }
  };

  public static final InMemCompOpEvaluator STRING_CONTAINS = new InMemCompOpEvaluatorBase<CompOpStringContains, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpStringContains compOp, String o1, String o2) {
      return CompareUtil.indexOf(o1, o2, compOp.isIgnoreCase(), compOp.isIgnoreSpaces()) != -1;
    }
  };

  public static final InMemCompOpEvaluator STRING_NOT_CONTAINS = new InMemCompOpEvaluatorBase<CompOpStringNotContains, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpStringNotContains compOp, String o1, String o2) {
      return CompareUtil.indexOf(o1, o2, compOp.isIgnoreCase(), compOp.isIgnoreSpaces()) == -1;
    }
  };

  public static final InMemCompOpEvaluator IN = new InMemCompOpEvaluatorBase<CompOpIn, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpIn compOp, Object o1, Object o2) {
      if (! (o2 instanceof Collection)) {
        throw new IllegalArgumentException("The IN operator expects a Collection as parameter.");
      }
      return ((Collection<?>)o2).contains(o1);
    }
  };


}
