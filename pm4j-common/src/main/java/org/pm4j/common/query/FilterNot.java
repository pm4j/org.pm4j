package org.pm4j.common.query;

import org.apache.commons.lang.ObjectUtils;


/**
 * A logical NOT condition.
 * <p>
 * Limitation: It currently can't be used as a hash map key. Because it is mutable.
 *
 * @author olaf boede
 */
public class FilterNot implements FilterExpression {

  private static final long serialVersionUID = 1L;
  private FilterExpression baseExpression;

  public FilterNot(FilterExpression baseExpression) {
    this.setBaseExpression(baseExpression);
  }

  public FilterExpression getBaseExpression() {
    return baseExpression;
  }

  public void setBaseExpression(FilterExpression baseExpression) {
    this.baseExpression = baseExpression;
  }

  @Override
  public String toString() {
    return "NOT(" + baseExpression + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FilterNot)) {
      return false;
    }
    return ObjectUtils.equals(baseExpression, ((FilterNot)obj).baseExpression);
  }
}
