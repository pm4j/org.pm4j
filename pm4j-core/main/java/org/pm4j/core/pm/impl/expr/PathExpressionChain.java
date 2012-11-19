package org.pm4j.core.pm.impl.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.AttributeExpr;
import org.pm4j.common.expr.ConcatExpr;
import org.pm4j.common.expr.ExprBase;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.ExprExecExeption;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.MethodCallExpr;
import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.OptionalExpression;
import org.pm4j.common.expr.ScalarExpr;
import org.pm4j.common.expr.ThisExpr;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.expr.parser.ParseException;

public class PathExpressionChain extends ExprBase<ExprExecCtxt> {

  private OptionalExpression[] chain;

  public PathExpressionChain(List<OptionalExpression> exprChain) {
    this.chain = exprChain.toArray(new OptionalExpression[exprChain.size()]);
  }

  public PathExpressionChain(OptionalExpression... chain) {
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
      PathExpressionChain restPathExprChain = new PathExpressionChain(restChain);
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
   * @param s The string to parse.
   * @param isStartAttrAllowed
   *          Defines if the first expression part may address a field.
   *          <p>
   *          Is used to prevent initializaion loops for injected fields that
   *          use the name of a referenced variable.
   * @return The parsed expression or <code>null</code> if there was an empty
   *         string to parse.
   */
  // TODO: boolean parameter seems to be obsolete, since variables are
  //       addressed explicitely
  public static Expression parse(String s, boolean isStartAttrAllowed) {
    return StringUtils.isEmpty(s)
                ? ThisExpr.INSTANCE
                : parse(new ParseCtxt(s), isStartAttrAllowed);
  }

  public static Expression parse(ParseCtxt ctxt) {
    return parse(ctxt, true);
  }

  /**
   * @param ctxt
   *          The current parse context (text and parse position).
   * @param isStartAttrAllowed
   *          Defines if the first expression part may address a field.
   *          <p>
   *          Is used to prevent initialization loops for injected fields that
   *          use the name of a referenced variable.
   * @return The parsed expression or <code>null</code> if there was an empty
   *         string to parse.
   */
  public static Expression parse(ParseCtxt ctxt, boolean isStartAttrAllowed) {
    Expression e = parseOneExpr(ctxt, isStartAttrAllowed);
    if (e == null || ctxt.isDone()) {
      return e;
    }

    List<Expression> eList = new ArrayList<Expression>();
    eList.add(e);
    while (ctxt.skipBlanks().readOptionalChar('+')) {
      e = parseOneExpr(ctxt, isStartAttrAllowed);
      eList.add(e);
    }
    return new ConcatExpr(eList);
  }


  private static Expression parseOneExpr(ParseCtxt ctxt, boolean isStartAttrAllowed) {
    Expression basicExpr = ScalarExpr.parse(ctxt);
    if (basicExpr != null) {
      return basicExpr;
    }

    OptionalExpression startExpr = MethodCallExpr.parse(ctxt);
    if (startExpr == null) {
      // allow expressions to start with 'this'.
      startExpr = ThisExpr.parse(ctxt);

      // Special handling that allows to forbid attribute usage as expression starter.
      // Useful to prevent conflicts between attribute names and names of named objects.
      if (startExpr == null) {
        startExpr = isStartAttrAllowed
            ? PmVariableOrAttributeExpr.parse(ctxt)
            : PmVariableExpr.parse(ctxt);
      }
    }

    if (startExpr != null) {
      List<OptionalExpression> exprList = new ArrayList<OptionalExpression>();
      exprList.add(startExpr);
      while (ctxt.testAndReadChar('.')) {
        OptionalExpression r = MethodCallExpr.parse(ctxt);
        if (r == null) {
          r = AttributeExpr.parse(ctxt);
        }

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
        return new PathExpressionChain(exprList);
      }
    }
    else {
      return null;
    }
  }

}
