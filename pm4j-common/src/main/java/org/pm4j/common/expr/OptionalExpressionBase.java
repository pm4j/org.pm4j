package org.pm4j.common.expr;

import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;

public abstract class OptionalExpressionBase<CTXT extends ExprExecCtxt> extends ExprBase<CTXT> implements OptionalExpression {

  protected final NameWithModifier nameWithModifier;

  public OptionalExpressionBase(ParseCtxt ctxt, NameWithModifier nameWithModifier) {
    super(ctxt);
    assert nameWithModifier != null;
    this.nameWithModifier = nameWithModifier;
  }

  public OptionalExpressionBase(SyntaxVersion syntaxVersion, NameWithModifier nameWithModifier) {
    super(syntaxVersion);
    assert nameWithModifier != null;
    this.nameWithModifier = nameWithModifier;
  }

  /**
   * @return the nameWithModifier
   */
  public NameWithModifier getNameWithModifier() {
    return nameWithModifier;
  }

  @Override
  public boolean hasNameModifier(Modifier nameModifier) {
    return nameWithModifier.getModifiers().contains(nameModifier);
  }

  @Override
  public String toString() {
    return nameWithModifier.toString();
  }

}
