package org.pm4j.common.expr;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.parser.ParseCtxt;

/**
 * Encapsulates the parsing logic for all supported scalar value expressions.
 *
 * @author olaf boede
 */
public class ScalarExpr {

  public static Expression parse(ParseCtxt ctxt) {
    Expression e = StringExpr.parse(ctxt);
    if (e == null) {
      e = NumberExpr.parse(ctxt);
    }
    if (e == null) {
      int oriPos = ctxt.getPos();
      String s = ctxt.skipBlanks().readNameString();
      e = parseReservedWord(ctxt, s);
      if (e == null) {
        ctxt.setPos(oriPos);
      }
    }
    return e;
  }

  private static Expression parseReservedWord(ParseCtxt ctxt, String s) {
    if (StringUtils.isEmpty(s)) {
      return null;
    }
    if (s.equals("true")) {
      return new BooleanExpr(ctxt, Boolean.TRUE);
    }
    if (s.equals("false")) {
      return new BooleanExpr(ctxt, Boolean.FALSE);
    }
    if (s.equals("null")) {
      return new NullExpr(ctxt);
    }
    // no match
    return null;
  }

  public static class StringExpr extends ExprBase<ExprExecCtxt> {

    private String stringValue;

    public StringExpr(ParseCtxt ctxt, String stringValue) {
      super(ctxt);
      this.stringValue = stringValue;
    }

    @Override
    protected Object execImpl(ExprExecCtxt ctxt) {
      return stringValue;
    }

    public static StringExpr parse(ParseCtxt ctxt) {
      int oriPos = ctxt.getPos();
      if (ctxt.skipBlanks().readOptionalChar('\'')) {
        // TODO: escaping not yet supported.
        String s = ctxt.readTill('\'');
        if (ctxt.isOnChar('\'')) {
          ctxt.readCharAndAdvance();
          return new StringExpr(ctxt, s);
        }
      }
      // no match:
      ctxt.setPos(oriPos);
      return null;
    }
  }

  public static class BooleanExpr extends ExprBase<ExprExecCtxt> {

    private Boolean value;

    public BooleanExpr(ParseCtxt ctxt, Boolean value) {
      super(ctxt);
      this.value = value;
    }

    @Override
    protected Object execImpl(ExprExecCtxt ctxt) {
      return value;
    }
  }

  public static class NullExpr extends ExprBase<ExprExecCtxt> {

    public NullExpr(ParseCtxt ctxt) {
      super(ctxt);
    }

    @Override
    protected Object execImpl(ExprExecCtxt ctxt) {
      return null;
    }
  }

  public static class NumberExpr extends ExprBase<ExprExecCtxt> {

    private Number number;

    public NumberExpr(ParseCtxt ctxt, Number number) {
      super(ctxt);
      this.number = number;
    }

    @Override
    protected Object execImpl(ExprExecCtxt ctxt) {
      return number;
    }

    public static NumberExpr parse(ParseCtxt ctxt) {
      int oriPos = ctxt.getPos();
      boolean hasDot = false, otherCharRead = false;
      StringBuilder digits = new StringBuilder();

      ctxt.skipBlanks();
      char ch = ' ';
      do {
        ch = ctxt.readCharAndAdvance();

        switch (ch) {
          case '-':
            if (digits.length() == 0) {
              digits.append(ch);
            } else {
              otherCharRead = true;
            }
            break;
          case '.':
            // no double dots an no dots at fist position
            if (hasDot || digits.length() == 0) {
              otherCharRead = true;
            } else {
              digits.append(ch);
              hasDot = true;
            }
            break;
          default:
            if (Character.isDigit(ch)) {
              digits.append(ch);
            } else {
              otherCharRead = true;
            }
            break;
        }
      } while (!ctxt.isDone() && !otherCharRead);

      if (digits.length() > 0) {
        // check for a type indicator
        switch (ch) {
          case 'l':
          case 'L':
            return new NumberExpr(ctxt, new Long(digits.toString()));
          case 'd':
          case 'D':
            return new NumberExpr(ctxt, new Double(digits.toString()));
          case 'f':
          case 'F':
            return new NumberExpr(ctxt, new Float(digits.toString()));
          default:
            // put the last read character back. It was not a type indicator:
            if (otherCharRead)
              ctxt.setPos(ctxt.getPos() - 1);

            return hasDot
                ? new NumberExpr(ctxt, new Double(digits.toString()))
                : new NumberExpr(ctxt, new Integer(digits.toString()));
        }
      }

      // no number:
      ctxt.setPos(oriPos);
      return null;
    }

  }

}
