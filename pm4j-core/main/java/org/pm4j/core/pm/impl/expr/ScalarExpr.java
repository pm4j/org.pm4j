package org.pm4j.core.pm.impl.expr;

import java.util.Map;

import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.core.pm.impl.expr.parser.ParseCtxt;

/**
 * Encapsulates the parsing logic for all supported scalar value expressions.
 * 
 * @author olaf boede
 */
public class ScalarExpr {

  private static final Map<String, Expression> RESERVED_WORD_TO_EXPR_MAP = MapUtil.makeHashMap(
      "true", new BooleanExpr(Boolean.TRUE),
      "false", new BooleanExpr(Boolean.FALSE),
      "null", new NullExpr()
      );
  
  public static Expression parse(ParseCtxt ctxt) {
    Expression e = StringExpr.parse(ctxt);
    if (e == null) {
      e = NumberExpr.parse(ctxt);
    }
    if (e == null) {
      int oriPos = ctxt.getPos();
      String s = ctxt.skipBlanks().readNameString();
      e = RESERVED_WORD_TO_EXPR_MAP.get(s);
      if (e == null) {
        ctxt.setPos(oriPos);
      }
    }
    return e;
  }

  public static class StringExpr extends ExprBase<ExprExecCtxt> {

    private String stringValue;

    public StringExpr(String stringValue) {
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
          return new StringExpr(s);
        }
      }
      // no match:
      ctxt.setPos(oriPos);
      return null;
    }
  }

  public static class BooleanExpr extends ExprBase<ExprExecCtxt> {

    private Boolean value;

    public BooleanExpr(Boolean value) {
      this.value = value;
    }

    @Override
    protected Object execImpl(ExprExecCtxt ctxt) {
      return value;
    }
  }

  public static class NullExpr extends ExprBase<ExprExecCtxt> {
    @Override
    protected Object execImpl(ExprExecCtxt ctxt) {
      return null;
    }
  }

  public static class NumberExpr extends ExprBase<ExprExecCtxt> {

    private Number number;

    public NumberExpr(Number number) {
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
            return new NumberExpr(new Long(digits.toString()));
          case 'd':
          case 'D':
            return new NumberExpr(new Double(digits.toString()));
          case 'f':
          case 'F':
            return new NumberExpr(new Float(digits.toString()));
          default:
            // put the last read character back. It was not a type indicator:
            if (otherCharRead)
              ctxt.setPos(ctxt.getPos() - 1);

            return hasDot ? new NumberExpr(new Double(digits.toString())) : new NumberExpr(new Integer(
                digits.toString()));
        }
      }

      // no number:
      ctxt.setPos(oriPos);
      return null;
    }

  }

}
