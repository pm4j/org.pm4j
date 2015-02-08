package org.pm4j.common.expr;

import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.expr.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class PathExpressionChain extends ExprBase<ExprExecCtxt> {

  private OptionalExpression[] chain;

  public PathExpressionChain(ParseCtxt ctxt, List<OptionalExpression> exprChain) {
    super(ctxt);
    this.chain = exprChain.toArray(new OptionalExpression[exprChain.size()]);
  }

  protected PathExpressionChain(SyntaxVersion syntaxVersion, OptionalExpression... chain) {
    super(syntaxVersion);
    this.chain = chain;
  }

  @Override
  protected Object execImpl(ExprExecCtxt ctxt) {
    for (int i=0; i<chain.length; ++i) {
      OptionalExpression r = chain[i];
      r.exec(ctxt);
      if (ctxt.getCurrentValue() == null) {
        if (i != chain.length-1 &&
            ! r.hasNameModifier(Modifier.OPTIONAL)) {
          throw new ExprExecExeption(ctxt, "Mandatory expression returns 'null'.");
        }
        break;
      }
    }
    return ctxt.getCurrentValue();
  }

  @Override
  protected void execAssignImpl(ExprExecCtxt ctxt, Object value) {
    int lastPos = chain.length-1;
    for (int i=0; i<lastPos; ++i) {
      OptionalExpression r = chain[i];
      r.exec(ctxt);

      if (ctxt.getCurrentValue() == null) {
        if (r.hasNameModifier(Modifier.OPTIONAL)) {
          // not reachable optional path.
          return;
        }
        else {
          throw new ExprExecExeption(ctxt, "Mandatory expression returns 'null'.");
        }
      }

    }
    chain[lastPos].execAssign(ctxt, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(40);
    for (Expression e : chain) {
      if (sb.length() > 0)
        sb.append('.');
      sb.append(e.toString());
    }
    return sb.toString();
  }

  /**
   * @param exprString
   *         The expression string to parse.
   * @return The parsed expression or <code>null</code> if there was an empty
   *         string to parse.
   */
  public static Expression parse(String exprString) {
    return parse(new ParseCtxt(exprString));
  }

  /**
   * @param ctxt
   *          The current parse context (text and parse position).
   * @return The parsed expression or <code>null</code> if there was an empty
   *         string to parse.
   */
  public static Expression parse(ParseCtxt ctxt) {
    Expression e = parseOneExpr(ctxt);
    if (e == null || ctxt.isDone()) {
      return e;
    }

    // checks for '+' combined expressions
    List<Expression> eList = new ArrayList<Expression>();
    eList.add(e);
    while (ctxt.skipBlanks().readOptionalChar('+')) {
      e = parseOneExpr(ctxt);
      eList.add(e);
    }

    return (eList.size() == 1)
        ? eList.get(0)
        : new ConcatExpr(ctxt, eList);
  }


  private static Expression parseOneExpr(ParseCtxt ctxt) {
    Expression basicExpr = ScalarExpr.parse(ctxt);
    if (basicExpr != null) {
      return basicExpr;
    }

    // allow expressions to start with 'this'.
    OptionalExpression startExpr = ThisExpr.parse(ctxt);
    if (startExpr == null) {
      startExpr = parseMethodCallOrAttr(ctxt);
    }

    if (startExpr != null) {
      List<OptionalExpression> exprList = new ArrayList<OptionalExpression>();
      exprList.add(startExpr);
      while (ctxt.testAndReadChar('.')) {
        OptionalExpression r = parseMethodCallOrAttr(ctxt);

        if (r != null) {
          exprList.add(r);
        }
        else {
          throw new ParseException(ctxt, "Path expression expected after the dot character.");
        }
      }

      if (exprList.size() == 1) {
        return startExpr;
      }
      else {
        return new PathExpressionChain(ctxt, exprList);
      }
    }
    else {
      return null;
    }
  }

  private static OptionalExpression parseMethodCallOrAttr(ParseCtxt ctxt) {
    OptionalExpression expr = MethodCallExpr.parse(ctxt);
    if (expr == null) {
      expr = AttributeExpr.parse(ctxt);
    }
    return expr;
  }

}
