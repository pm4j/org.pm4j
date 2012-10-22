package org.pm4j.common.query.inmem;

import org.pm4j.common.query.CompOp;

public abstract class InMemCompOpEvaluatorBase<T_COMP_OP extends CompOp, T_VALUE> implements InMemCompOpEvaluator {

  @SuppressWarnings("unchecked")
  @Override
  public boolean eval(InMemQueryEvaluator<?> ctxt, CompOp compOp, Object o1, Object o2) {
    return evalImpl(ctxt, (T_COMP_OP) compOp, (T_VALUE)o1, (T_VALUE)o2);
  }

  protected abstract boolean evalImpl(InMemQueryEvaluator<?> ctxt, T_COMP_OP compOp, T_VALUE o1, T_VALUE o2);

}
