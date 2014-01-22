package org.pm4j.common.query;


/**
 * An in memory only evaluated {@link QueryExpr}.
 * <p>
 * ATTENTION: It can only be used for in-memory based query evaluation.
 *
 * @author Olaf Boede
 */
public interface QueryExprInMemCondition<T_ITEM> extends QueryExpr {

  /**
   * Evaluates in memory if the given item matches this filter definition.
   *
   * @param item The item to check.
   * @return <code>true</code> if the item matches.
   */
  boolean eval(T_ITEM item);
}
