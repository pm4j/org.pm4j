package org.pm4j.core.pm.impl.expr;

import org.pm4j.common.expr.ExprExecExeption;
import org.pm4j.common.expr.NameWithModifier;
import org.pm4j.common.expr.OptionalExpression;
import org.pm4j.common.expr.OptionalExpressionBase;
import org.pm4j.common.expr.ThisExpr;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;

public class PmVariableExpr extends OptionalExpressionBase<PmExprExecCtxt> {

  public PmVariableExpr(ParseCtxt ctxt, NameWithModifier nameWithModifier) {
	// XXX oboede: why was that cloned here?
    super(ctxt, nameWithModifier.clone());
    // Ensure that the flag is set, to provide explicit modifiers for
    // debugging and logging.
    this.nameWithModifier.setVariable(true);
  }

  public PmVariableExpr(SyntaxVersion syntaxVersion, NameWithModifier nameWithModifier) {
    super(syntaxVersion, nameWithModifier.clone());
    // Ensure that the flag is set, to provide explicit modifiers for
    // debugging and logging.
    this.nameWithModifier.setVariable(true);
  }

  @Override
  protected Object execImpl(PmExprExecCtxt ctxt) {
    PmObject pm = ctxt.getPm();
    if (pm == null) {
      throw new ExprExecExeption(ctxt, "PM in expression context is 'null'.");
    }

    return PmExpressionApi.findNamedObject(pm, nameWithModifier.getName());
  }

  public static OptionalExpression parse(ParseCtxt ctxt) {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(ctxt);
    return (n != null)
              ? new PmVariableExpr(ctxt, n)
              : new ThisExpr(ctxt);
  }

}
