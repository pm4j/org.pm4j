package org.pm4j.common.query.inmem;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.CompOpEvaluator;

/**
 * Basic interface for in-memory evaluators.
 * <p>
 * Provides an {@link #equals(Object)} method signature to check the condition.
 *
 * @author olaf boede
 */
public interface InMemCompOpEvaluator extends CompOpEvaluator {

  /**
   * Evaluates the operator using the given in-memory objects.
   *
   * @param ctxt
   * @param compOp
   * @param o1
   * @param o2
   * @return
   */
  boolean eval(InMemQueryEvaluator<?> ctxt, CompOp compOp, Object o1, Object o2);

}
