package org.pm4j.common.query.inmem;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.CompOpEvaluator;

public interface InMemCompOpEvaluator extends CompOpEvaluator {

  boolean eval(InMemQueryEvaluator<?> ctxt, CompOp compOp, Object o1, Object o2);

}
