package org.pm4j.core.pm.impl.expr;


/**
 * Interface for an algorithm that evaluates an expression.
 *
 * @author olaf boede
 */
public interface Expression {


  Object exec(ExprExecCtxt ctxt);

  /**
   * Value assignment operation. Is supported by path expressions that reference
   * attributes that provide public modification access (setter or public
   * non-final declaration).
   *
   * @param ctxt
   *          Run time context data for the expression evaluation.
   * @param value
   *          The value to set to the field, addressed by the path.
   */
  void execAssign(ExprExecCtxt ctxt, Object value);

}
