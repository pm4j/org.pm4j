package org.pm4j.common.query.inmem;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpStringContains;
import org.pm4j.common.query.CompOpStringIsEmpty;
import org.pm4j.common.query.CompOpStringNotContains;
import org.pm4j.common.query.CompOpStringNotIsEmpty;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.util.CompareUtil;

public class InMemCompOpEvaluators {

  public static final InMemCompOpEvaluator EQUALS = new InMemCompOpEvaluatorBase<CompOpEquals, Object>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpEquals compOp, Object o1, Object o2) {
      return ObjectUtils.equals(o1, o2);
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
      return o1 == null;
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

  public static final InMemCompOpEvaluator STRING_IS_EMPTY = new InMemCompOpEvaluatorBase<CompOpStringIsEmpty, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpStringIsEmpty compOp, String o1, String o2) {
      return StringUtils.isEmpty(o1);
    }
  };

  public static final InMemCompOpEvaluator STRING_IS_NOT_EMPTY = new InMemCompOpEvaluatorBase<CompOpStringNotIsEmpty, String>() {
    @Override
    protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpStringNotIsEmpty compOp, String o1, String o2) {
      return ! StringUtils.isEmpty(o1);
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
}
