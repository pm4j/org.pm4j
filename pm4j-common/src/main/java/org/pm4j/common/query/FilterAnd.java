package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.util.CompareUtil;


/**
 * A logical AND condition. It can combine any number of {@link FilterExpression}s.
 * <p>
 * Limitation: It currently can't be used as a hash map key. Because it is mutable.
 *
 * @author olaf boede
 */
public class FilterAnd implements FilterExpression {

  private static final long serialVersionUID = 1L;
  private List<FilterExpression> expressions;

  public FilterAnd(FilterExpression... expressions) {
    this(Arrays.asList(expressions));
  }

  public FilterAnd(Collection<? extends FilterExpression> expressions) {
    this.expressions = new ArrayList<FilterExpression>(expressions);
  }

  public void add(FilterExpression expression) {
    this.expressions.add(expression);
  }

  public List<FilterExpression> getExpressions() {
    return expressions;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (FilterExpression e : expressions) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(e);
    }
    sb.append(")");
    return "AND(" + sb;
  }

  /**
   * This class has no parallel {@link #hashCode()} implentation because it is not immutable.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof FilterAnd)) {
      return false;
    }
    FilterAnd rhs = (FilterAnd) obj;
    return CompareUtil.equalLists(this.expressions, rhs.expressions);
  }

}
