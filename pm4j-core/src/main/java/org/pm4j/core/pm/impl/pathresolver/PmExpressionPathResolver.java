package org.pm4j.core.pm.impl.pathresolver;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.expr.PathExpressionChain;
import org.pm4j.core.pm.impl.expr.PmExprExecCtxt;

/**
 * A path resolver that uses an {@link Expression}.
 * <p>
 * Provides a simplified interface for getting an setting values by using
 * expressions.
 *
 * @author olaf boede
 */
public class PmExpressionPathResolver extends PathResolverBase {

  private Expression expression;

  /**
   * @param exprString The string to parse.
   * @param syntaxVersion The expression syntax version to use.
   */
  public static PathResolver parse(String exprString, SyntaxVersion syntaxVersion) {
    return StringUtils.isEmpty(exprString)
            ? PassThroughPathResolver.INSTANCE
            : new PmExpressionPathResolver(PathExpressionChain.parse(exprString, syntaxVersion), syntaxVersion);
  }

  /**
   * @param parseCtxt The parse string context.
   */
  public static PathResolver parse(ParseCtxt parseCtxt) {
    parseCtxt.skipBlanks();
    return parseCtxt.isDone()
            ? PassThroughPathResolver.INSTANCE
            : new PmExpressionPathResolver(PathExpressionChain.parse(parseCtxt), parseCtxt.getSyntaxVersion());
  }

  protected PmExpressionPathResolver(Expression expression, SyntaxVersion syntaxVersion) {
    super(syntaxVersion);
    this.expression = expression;
  }

  @Override
  public Object getValue(Object startObj) {
    return getValue(startObj, startObj);
  }

  @Override
  public Object getValue(Object pmCtxt, Object startObj) {
    if (startObj == null) {
      return null;
    }

    ExprExecCtxt ctxt = new PmExprExecCtxt((PmObject)pmCtxt, startObj);
    expression.exec(ctxt);
    return ctxt.getCurrentValue();
  }

  @Override
  public void setValue(Object startObj, Object value) {
    ExprExecCtxt ctxt = new PmExprExecCtxt((PmObject)startObj);
    expression.execAssign(ctxt, value);
  }

  @Override
  public String toString() {
    return expression.toString();
  }

}
