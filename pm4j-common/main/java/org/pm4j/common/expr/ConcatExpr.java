package org.pm4j.common.expr;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.util.collection.ListUtil;

/**
 * A string concatination expression.
 * <p>
 * Supports one or more arguments.
 * <p>
 * Example expression: <pre>'Good morning ' + getUserName() + '!'</pre>
 * <p>
 * The parsing algorithm is located in {@link PathExpressionChain#parse(ParseCtxt, boolean)}.
 *
 * @author olaf boede
 */
public class ConcatExpr extends ExprBase<ExprExecCtxt>{

  private final Expression[] argList;

  public ConcatExpr(List<Expression> argList) {
    this.argList = argList.toArray(new Expression[argList.size()]);
  }

  @Override
  protected Object execImpl(ExprExecCtxt ctxt) {
    StringBuilder sb = new StringBuilder();
    for (Expression e : argList) {
      ExprExecCtxt paramCtxt = ctxt.makeSubCtxt();
      Object v = e.exec(paramCtxt);
      sb.append(ObjectUtils.toString(v));
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return StringUtils.join(argList, "+");
  }

}
