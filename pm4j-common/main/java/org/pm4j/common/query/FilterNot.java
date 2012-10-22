package org.pm4j.common.query;


public class FilterNot implements FilterExpression {

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


}
