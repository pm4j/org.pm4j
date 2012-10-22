package org.pm4j.common.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * The execution context for expressions.<br>
 * Holds the current value of the expression.
 * <p>
 * For exception reporting and debugging it provides a history of expression
 * execution states.
 *
 * @author olaf boede
 */
public class ExprExecCtxt implements Cloneable {

  private Object currentValue;
  private final Object startValue;
  private List<HistoryItem> execHistory = new ArrayList<HistoryItem>();
  private Expression startExpr;
  private Expression currentExpr;

  /**
   * @param startObject The value to start the expression evaluation with.
   */
  public ExprExecCtxt(Object startObject) {
    this.currentValue = this.startValue = startObject;
  }

  @Override
  public ExprExecCtxt clone() {
    try {
      ExprExecCtxt clone = (ExprExecCtxt) super.clone();
      clone.execHistory = new ArrayList<HistoryItem>(this.execHistory);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  public void setCurrentExpr(Expression expr) {
    if (startExpr == null) {
      startExpr = expr;
    }
    currentExpr = expr;
  }

  /**
   * @return The result of the last expression execution.
   */
  public Object getCurrentValue() {
    return currentValue;
  }

  /**
   * Gets called whenever an expression has calculated a new value state.
   *
   * @param expr
   *          The just processed expression.
   * @param newObject
   *          The result of the expression.
   */
  public void setCurrentValue(Expression expr, Object newObject) {
    execHistory.add(new HistoryItem(expr, newObject));
    this.currentValue = newObject;
  }

  /**
   * @return The value, expression evaluation was started with.
   */
  public Object getStartValue() {
    return startValue;
  }

  /**
   * @return The expression execution history.
   */
  public List<HistoryItem> getExecHistory() {
    return Collections.unmodifiableList(execHistory);
  }

  /**
   * An executed expression and it's result.
   */
  public static class HistoryItem {
    public final Expression expression;
    public final Object value;
    public HistoryItem(Expression expression, Object value) {
      this.expression = expression;
      this.value = value;
    }
  }

  public Expression getStartExpr() {
    return startExpr;
  }

  public Expression getCurrentExpr() {
    return currentExpr;
  }

  /**
   * Generates a sub context that may be used to evaluate a separate expression.
   * For example an expression that evaluates a method call parameter value.
   *
   * @return A separate subcontext instance.
   */
  public ExprExecCtxt makeSubCtxt() {
    return new ExprExecCtxt(startValue);
  }

}
