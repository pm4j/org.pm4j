package org.pm4j.common.query;

import org.apache.commons.lang.ObjectUtils;


/**
 * A logical NOT condition.
 * <p>
 * Limitation: It currently can't be used as a hash map key. Because it is mutable.
 *
 * @author olaf boede
 */
public class QueryExprNot implements QueryExpr {

  private static final long serialVersionUID = 1L;
  private QueryExpr baseExpression;

  public QueryExprNot(QueryExpr baseExpression) {
    this.setBaseExpression(baseExpression);
  }

  public QueryExpr getBaseExpression() {
    return baseExpression;
  }

  public void setBaseExpression(QueryExpr baseExpression) {
    this.baseExpression = baseExpression;
  }

  @Override
  public String toString() {
    return "NOT(" + baseExpression + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof QueryExprNot)) {
      return false;
    }
    return ObjectUtils.equals(baseExpression, ((QueryExprNot)obj).baseExpression);
  }
}
