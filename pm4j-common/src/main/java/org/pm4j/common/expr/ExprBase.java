package org.pm4j.common.expr;

import org.pm4j.common.expr.parser.ParseCtxt;

/**
 * Basic implementation of some common expression evaluation functionality.
 *
 * @author olaf boede
 *
 * @param <CTXT> Used type of expression execution context information.
 */
public abstract class ExprBase<CTXT extends ExprExecCtxt> implements Expression {

  private final SyntaxVersion syntaxVersion;

  public ExprBase(ParseCtxt ctxt) {
    this.syntaxVersion = ctxt.getSyntaxVersion();
  }

  public ExprBase(SyntaxVersion syntaxVersion) {
    this.syntaxVersion = syntaxVersion;
  }

  protected String getPathName() {
    return toString();
  }

  @Override @SuppressWarnings("unchecked")
  public Object exec(ExprExecCtxt ctxt) {
    ctxt.setCurrentExpr(this);
    Object result = execImpl((CTXT)ctxt);
    ctxt.setCurrentValue(this, result);
    return result;
  }

  protected abstract Object execImpl(CTXT ctxt);

  protected void execAssignImpl(CTXT ctxt, Object value) {
    throw new ExprExecExeption(ctxt, "Value assignement is not supported by this expression.");
  }

  /**
   * Default implementation just throws an exception.
   * <p>
   * Subclasses that allow value assignment should override this method.
   */
  @Override @SuppressWarnings("unchecked")
  public final void execAssign(ExprExecCtxt ctxt, Object value) {
    execAssignImpl((CTXT)ctxt, value);
  }

  /**
   * @return the syntaxVersion
   */
  public SyntaxVersion getSyntaxVersion() {
    return syntaxVersion;
  }

}
