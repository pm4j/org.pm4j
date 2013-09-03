package org.pm4j.core.pm.impl.pathresolver;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.core.pm.impl.expr.PathExpressionChain;

/**
 * Evaluates a path value based on an {@link Expression} implementation.
 *
 * @author olaf boede
 */
public class ExpressionPathResolver extends PathResolverBase {

  private final Expression expression;

  public static PathResolver parse(String exprString, SyntaxVersion syntaxVersion) {
    return StringUtils.isEmpty(exprString)
        ? PassThroughPathResolver.INSTANCE
        : new ExpressionPathResolver(syntaxVersion, exprString);
  }

  public static PathResolver parse(String exprString) {
    return parse(exprString, SyntaxVersion.VERSION_2);
  }

  protected ExpressionPathResolver(SyntaxVersion syntaxVersion, String exprString) {
    super(syntaxVersion);
    this.expression = PathExpressionChain.parse(exprString, syntaxVersion);
  }

  @Override
  public Object getValue(Object startObj) {
    ExprExecCtxt ctxt = new ExprExecCtxt(startObj);
    expression.exec(ctxt);
    return ctxt.getCurrentValue();
  }

  /**
   * Ignores the context parameter.
   */
  @Override
  public Object getValue(Object pmCtxt, Object startObj) {
    return getValue(startObj);
  }

  @Override
  public void setValue(Object startObj, Object value) {
    ExprExecCtxt ctxt = new ExprExecCtxt(startObj);
    expression.execAssign(ctxt, value);
  }

  @Override
  public String toString() {
    return expression.toString();
  }

}
