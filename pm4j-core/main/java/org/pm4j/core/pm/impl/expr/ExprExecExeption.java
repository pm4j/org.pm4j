package org.pm4j.core.pm.impl.expr;

import org.pm4j.core.pm.impl.expr.ExprExecCtxt.HistoryItem;

public class ExprExecExeption extends RuntimeException {

  private static final long serialVersionUID = -6508877423633015467L;

  public ExprExecExeption(ExprExecCtxt ctxt, String msg) {
    this(ctxt, msg, null);
  }

  public ExprExecExeption(ExprExecCtxt ctxt, String msg, Throwable cause) {
    super(makeMsg(ctxt, msg), cause);
  }

  private static String makeMsg(ExprExecCtxt ctxt, String msg) {
    StringBuilder sb = new StringBuilder(100);
    sb.append(msg).append("\n");

    if (ctxt.getStartExpr() != null) {
      String startExprName = ctxt.getStartExpr().toString();
      sb.append("Expression: ")
        .append(startExprName)
        .append("\n");

      String currentExprName = ctxt.getCurrentExpr().toString();
      if (! startExprName.equals(currentExprName)) {
        sb.append("  Currently processed expression part: ")
        .append(currentExprName)
        .append("\n");
      }
    }


    if (ctxt.getExecHistory().size() > 0) {
      sb.append("Execution history:\n")
        .append("  Start value: ")
        .append(ctxt.getStartValue() != null
                  // XXX: only the class info, since toString may provide errors
                  ? ctxt.getStartValue().getClass()
                  : "null");

      for (HistoryItem h : ctxt.getExecHistory()) {
        sb.append("\n  ").append(h.expression)
          .append(" -> ").append(h.value);
      }
    }

    return sb.toString();
  }

}
