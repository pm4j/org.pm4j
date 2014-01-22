package org.pm4j.common.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.util.CompareUtil;
import org.pm4j.common.util.collection.IterableUtil;


/**
 * A logical AND condition. It can combine any number of {@link QueryExpr}s.
 * <p>
 * Limitation: It currently can't be used as a hash map key. Because it is mutable.
 *
 * @author olaf boede
 */
public class QueryExprAnd implements QueryExpr, Serializable {

  /**
   * A static helper method that joins the given set of expressions to an and-combined expression.
   *
   * @param expressions a set of expressions. May be empty or <code>null</code> or may contain <code>null</code> items.
   * @return the resulting expression. May be <code>null</code> if the given parameter was empty or <code>null</code>.
   */
  public static QueryExpr joinToAnd(Collection<? extends QueryExpr> expressions) {
    List<? extends QueryExpr> list = IterableUtil.shallowCopyWithoutNulls(expressions);
    if (list.isEmpty()) {
      return null;
    } else if (list.size() == 1) {
      return list.iterator().next();
    } else {
      return new QueryExprAnd(list);
    }
  }

  /**
   * A static helper method that joins the given set of expressions to an and-combined expression.
   *
   * @param expressions a set of expressions. May be empty.
   * @return the resulting expression. May be <code>null</code> if the given parameter was empty or <code>null</code>.
   */
  public static QueryExpr joinToAnd(QueryExpr... expressions) {
    return joinToAnd(Arrays.asList(expressions));
  }

  private static final long serialVersionUID = 1L;
  private List<QueryExpr> expressions;

  public QueryExprAnd(QueryExpr... expressions) {
    this(Arrays.asList(expressions));
  }

  public QueryExprAnd(Collection<? extends QueryExpr> expressions) {
    this.expressions = new ArrayList<QueryExpr>(expressions);
  }

  public void add(QueryExpr expression) {
    this.expressions.add(expression);
  }

  public List<QueryExpr> getExpressions() {
    return expressions;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (QueryExpr e : expressions) {
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
    if (!(obj instanceof QueryExprAnd)) {
      return false;
    }
    QueryExprAnd rhs = (QueryExprAnd) obj;
    return CompareUtil.equalLists(this.expressions, rhs.expressions);
  }

}
