package org.pm4j.common.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.expr.parser.ParseException;

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

      if (r.hasNameModifier(Modifier.REPEATED)) {
        if (i == chain.length-1) {
          // Double check. Is also done within the parse operation
          throw new ExprExecExeption(ctxt, "A repeated expression can't be used as last part of an expression.");
        }

        ctxt = evalRepeated(ctxt, r, Arrays.copyOfRange(chain, i+1, chain.length));
        return ctxt.getCurrentValue();
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

      if (r.hasNameModifier(Modifier.REPEATED)) {
        ctxt = evalRepeated(ctxt, r, Arrays.copyOfRange(chain, i+1, chain.length-1));
        break;
      }
    }
    chain[lastPos].execAssign(ctxt, value);
  }

  // try to execute the rest of the chain.
  // if it fails: try to repeat r...
  private ExprExecCtxt evalRepeated(ExprExecCtxt ctxt, OptionalExpression repeatedExpr, OptionalExpression[] restChain) {
    try {
      PathExpressionChain restPathExprChain = new PathExpressionChain(getSyntaxVersion(), restChain);
      ExprExecCtxt clonedCtxt = ctxt.clone();
      restPathExprChain.exec(clonedCtxt);
      return clonedCtxt;
    }
    catch (RuntimeException e) {
      repeatedExpr.exec(ctxt);
      if (ctxt.getCurrentValue() == null) {
        throw new ExprExecExeption(ctxt, "The trailing expression part can't be resolved for any instance of the repeated expression that evaluates to 'null'.");
      }
      return evalRepeated(ctxt, repeatedExpr, restChain);
    }
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

      if (exprList.get(exprList.size()-1).hasNameModifier(Modifier.REPEATED)) {
        throw new ParseException(ctxt, "A repeated expression can't be used as last part of an expression.");
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
