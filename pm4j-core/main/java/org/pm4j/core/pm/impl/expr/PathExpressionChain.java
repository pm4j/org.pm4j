package org.pm4j.core.pm.impl.expr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.impl.expr.parser.ParseCtxt;
import org.pm4j.core.pm.impl.expr.parser.ParseException;

public class PathExpressionChain extends ExprBase<ExprExecCtxt> {

  private OptionalExpression[] chain;

  public PathExpressionChain(List<OptionalExpression> exprChain) {
    this.chain = exprChain.toArray(new OptionalExpression[exprChain.size()]);
  }

  @Override
  protected Object execImpl(ExprExecCtxt ctxt) {
    for (int i=0; i<chain.length; ++i) {
      OptionalExpression r = chain[i];
      r.exec(ctxt);
      if (ctxt.getCurrentValue() == null) {
        if (i != chain.length-1 &&
            ! r.isOptional()) {
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
        if (r.isOptional()) {
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
    Expression scalarExpr = ScalarExpr.parse(ctxt);
    if (scalarExpr != null) {
      return scalarExpr;
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
