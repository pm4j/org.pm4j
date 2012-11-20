package org.pm4j.core.pm.impl.pathresolver;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
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
   * @param isStartAttrAllowed
   *          Defines if the first expression part may address a field.
   *          <p>
   *          Is used to prevent initializaion loops for injected fields that
   *          use the name of a referenced variable.
   */
  public static PathResolver parse(String exprString, boolean isStartAttrAllowed) {
    return StringUtils.isEmpty(exprString)
            ? PassThroughPathResolver.INSTANCE
            : new PmExpressionPathResolver(exprString, isStartAttrAllowed);
  }

  public static PathResolver parse(ParseCtxt parseCtxt, boolean isStartAttrAllowed) {
    parseCtxt.skipBlanks();
    return parseCtxt.isDone()
            ? PassThroughPathResolver.INSTANCE
            : new PmExpressionPathResolver(parseCtxt, isStartAttrAllowed);
  }

  /**
   * @param exprString The string to parse.
   * @param isStartAttrAllowed
   *          Defines if the first expression part may address a field.
   *          <p>
   *          Is used to prevent initializaion loops for injected fields that
   *          use the name of a referenced variable.
   */
  protected PmExpressionPathResolver(String exprString, boolean isStartAttrAllowed) {
    this(PathExpressionChain.parse(exprString, isStartAttrAllowed));
  }

  protected PmExpressionPathResolver(ParseCtxt parseCtxt, boolean isStartAttrAllowed) {
    this(PathExpressionChain.parse(parseCtxt, isStartAttrAllowed));
  }

  protected PmExpressionPathResolver(Expression expression) {
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

}
