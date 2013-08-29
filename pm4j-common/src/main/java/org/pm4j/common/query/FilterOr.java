package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.util.CompareUtil;
import org.pm4j.common.util.collection.IterableUtil;


/**
 * A logical OR condition. It can combine any number of {@link FilterExpression}s.
 * <p>
 * Limitation: It currently can't be used as a hash map key. Because it is mutable.
 *
 * @author olaf boede
 */
public class FilterOr implements FilterExpression {

  /**
   * A static helper method that joins the given set of expressions to an or-combined expression.
   *
   * @param expressions a set of expressions. May be empty or <code>null</code> and may contain <code>null</code> items.
   * @return the resulting expression. May be <code>null</code> if the given parameter was empty or <code>null</code>.
   */
  public static FilterExpression joinToOr(Collection<? extends FilterExpression> expressions) {
    List<? extends FilterExpression> list = IterableUtil.shallowCopyWithoutNulls(expressions);
    if (list.isEmpty()) {
      return null;
    } else if (list.size() == 1) {
      return list.iterator().next();
    } else {
      return new FilterOr(list);
    }
  }

  /**
   * A static helper method that joins the given set of expressions to an or-combined expression.
   *
   * @param expressions a set of expressions. May be empty.
   * @return the resulting expression. May be <code>null</code> if the given parameter was empty or <code>null</code>.
   */
  public static FilterExpression joinToAnd(FilterExpression... expressions) {
    return joinToOr(Arrays.asList(expressions));
  }


  private static final long serialVersionUID = 1L;
  private List<FilterExpression> expressions;

  public FilterOr(FilterExpression... expressions) {
    this(Arrays.asList(expressions));
  }

  public FilterOr(Collection<? extends FilterExpression> expressions) {
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
    return "OR(" + sb;
  }

  /**
   * This class has currently no parallel {@link #hashCode()} implentation because it is not immutable.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof FilterOr)) {
      return false;
    }
    FilterOr rhs = (FilterOr) obj;
    return CompareUtil.equalLists(this.expressions, rhs.expressions);
  }


}
