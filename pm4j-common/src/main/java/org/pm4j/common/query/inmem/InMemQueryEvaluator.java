package org.pm4j.common.query.inmem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.PathExpressionChain;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryEvaluatorSet;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.util.collection.ListUtil;

/**
 * An algorithm that allows to filter in-memory items based on {@link QueryExpr}s.<br>
 * It also provides algorithms for item sorting based on a given {@link SortOrder}.
 * <p>
 * It's a stateful object, because it contains cached values.
 *
 * @param <T_ITEM> the type of handled items.
 *
 * @author olaf boede
 */
public class InMemQueryEvaluator<T_ITEM> {

  /** Evaluator set for the set of expressions and compare operators to handle. */
  private QueryEvaluatorSet evaluatorSet;

  /** Attribute path's are evaluated very often (especially when evaluating long lists). */
  private Map<QueryAttr, Expression> queryAttrToPathExpressionCache = new HashMap<QueryAttr, Expression>();

  /** A cache that may be used to prevent repeated evaluations. */
  private Map<String, Map<Object, Object>> cacheKeyToCacheMap = new HashMap<String, Map<Object,Object>>();

  public InMemQueryEvaluator() {
    this(InMemQueryEvaluatorSet.INSTANCE);
  }

  public InMemQueryEvaluator(QueryEvaluatorSet evaluatorSet) {
    this.evaluatorSet = evaluatorSet;
  }

  /**
   * Checks if the given item matches the given {@link QueryExpr}.
   *
   * @param item
   *          the item to check.
   * @param expr
   *          provides the filter criteria to check.
   * @return <code>true</code> if the item matches the filter criteria.
   */
  public boolean evaluate(Object item, QueryExpr expr) {
    InMemExprEvaluator ev = getExprEvaluator(expr);
    return ev.eval(this, item, expr);
  }

  /**
   * Provides a list that contains the filtered sub set.
   *
   * @param items
   *          the collection of all items.
   * @param expr
   *          the filter to apply. May be <code>null</code>.
   * @return the set of items that match the filter criteria.
   */
  public List<T_ITEM> evaluateSubSet(Collection<T_ITEM> items, QueryExpr expr) {
    if (items == null) {
      return new ArrayList<T_ITEM>();
    }

    if (expr == null) {
      return ListUtil.toList(items);
    }

    InMemExprEvaluator ev = getExprEvaluator(expr);
    List<T_ITEM> resultList = new ArrayList<T_ITEM>();
    for (T_ITEM i : items) {
      if (ev.eval(this, i, expr)) {
        resultList.add(i);
      }
    }

    return resultList;
  }

  /**
   * Provides a {@link Comparator} for the given {@link SortOrder}.
   * <p>
   * It considers multi-field sort order definitions.
   *
   * @param sortOrder
   *          the sort order. May be <code>null</code>.
   * @return the corresponding comparator. Is <code>null</code> if the given
   *         sort order was <code>null</code>.
   */
  public Comparator<T_ITEM> getComparator(SortOrder sortOrder) {
    if (sortOrder instanceof InMemSortOrder) {
      return new AttrPathComparator<T_ITEM>(this, (InMemSortOrder)sortOrder);
    } else if (sortOrder != null) {
      return new AttrPathComparator<T_ITEM>(this, new InMemSortOrder(sortOrder));
    } else {
      return null;
    }
  }

  /**
   * Sorts the given {@link Collection} according to the given {@link SortOrder}
   * .
   *
   * @param items
   *          the collection to sort. May be <code>null</code>.
   * @param sortOrder
   *          the sort order definition. May be <code>null</code>.
   * @return a list with sorted items. Is never <code>null</code>.
   */
  public List<T_ITEM> sort(Collection<T_ITEM> items, SortOrder sortOrder) {
    if (items == null || items.isEmpty()) {
      return new ArrayList<T_ITEM>();
    }

    if (sortOrder == null) {
      return new ArrayList<T_ITEM>(items);
    }
    else {
      @SuppressWarnings("unchecked")
      T_ITEM[] beanArray = (T_ITEM[]) items.toArray(new Object[items.size()]);
      Comparator<T_ITEM> comparator = getComparator(sortOrder);
      Arrays.sort(beanArray, comparator);
      return new ArrayList<T_ITEM>(Arrays.asList(beanArray));
    }
  }

  protected InMemExprEvaluator getExprEvaluator(QueryExpr expr) {
    return (InMemExprEvaluator) evaluatorSet.getExprEvaluator(expr);
  }

  protected InMemCompOpEvaluator getCompOpEvaluator(QueryExprCompare compareOperation) {
    return (InMemCompOpEvaluator) evaluatorSet.getCompOpEvaluator(compareOperation);
  }

  /**
   * Gets the specified attribute value from the item.<br>
   * Sub classes may define here other value resolution algorithms.
   */
  public Object getAttrValue(Object item, QueryAttr attr) {
    Expression expr = queryAttrToPathExpressionCache.get(attr);
    if (expr == null) {
      expr = PathExpressionChain.parse(attr.getPath());
      queryAttrToPathExpressionCache.put(attr, expr);
    }
    Object value = expr.getValue(item);
    return value;
  }

  /**
   * Provides a named cache.
   * <p>
   * Allows to define several comparator specific caches without key restrictions and
   * side effects to other comparators.
   *
   * @param cacheKey An identifier for the cache.
   * @return A map that can be used as a cache.
   */
  public Map<Object, Object> getCache(String cacheKey) {
    Map<Object, Object> cacheMap = cacheKeyToCacheMap.get(cacheKey);
    if (cacheMap == null) {
      cacheMap = new HashMap<Object, Object>();
      cacheKeyToCacheMap.put(cacheKey, cacheMap);
    }
    return cacheMap;
  }

  /**
   * Clears all cached items.
   */
  public void clearCaches() {
    cacheKeyToCacheMap.clear();
    queryAttrToPathExpressionCache.clear();
  }

  /**
   * A comparator that allows to compare based on an {@link InMemQueryEvaluator} an {@link InMemSortOrder}.
   * <p>
   * The {@link InMemQueryEvaluator} provides the attribute values to compare.
   *
   * @param <T> the type of items to sort.
   */
  static class AttrPathComparator<T> implements Comparator<T> {

    private final InMemQueryEvaluator<T> evaluatorCtxt;
    private final InMemSortOrder sortOrder;

    public AttrPathComparator(InMemQueryEvaluator<T> evaluatorCtxt, InMemSortOrder sortOrder) {
      assert evaluatorCtxt != null;
      assert sortOrder != null;

      this.evaluatorCtxt = evaluatorCtxt;
      this.sortOrder = sortOrder;
    }

    @Override
    public int compare(T o1, T o2) {
      int result = 0;
      InMemSortOrder so = sortOrder;
      
      while (so != null && result == 0) {
        QueryAttr d = so.getAttr();
        Object v1 = evaluatorCtxt.getAttrValue(o1, d);
        Object v2 = evaluatorCtxt.getAttrValue(o2, d);

        result = so.getComparator().compare(v1, v2);
        so = (InMemSortOrder) so.getNextSortOrder();
      }
      
      return result;
    }

  }


  /**
   * @return the evaluatorSet
   */
  public QueryEvaluatorSet getEvaluatorSet() {
    return evaluatorSet;
  }

}
