package org.pm4j.common.query.inmem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.PathExpressionChain;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryAttrMulti;
import org.pm4j.common.query.FilterCompare;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.QueryEvaluatorBase;
import org.pm4j.common.query.QueryEvaluatorSet;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.common.util.collection.MultiObjectValue;

/**
 * An algorithm that allows to filter in-memory items based on {@link FilterExpression}s.<br>
 * It also provides algorithms for item sorting based on a given {@link SortOrder}.
 *
 * @param <T_ITEM> the type of handled items.
 *
 * @author olaf boede
 */
public class InMemQueryEvaluator<T_ITEM> extends QueryEvaluatorBase {

  public InMemQueryEvaluator() {
    super(InMemQueryEvaluatorSet.INSTANCE);
  }

  public InMemQueryEvaluator(QueryEvaluatorSet evaluatorSet) {
    super(evaluatorSet);
  }

  /**
   * Checks if the given item matches the given {@link FilterExpression}.
   *
   * @param item
   *          the item to check.
   * @param expr
   *          provides the filter criteria to check.
   * @return <code>true</code> if the item matches the filter criteria.
   */
  public boolean evaluate(Object item, FilterExpression expr) {
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
  public List<T_ITEM> evaluateSubSet(Collection<T_ITEM> items, FilterExpression expr) {
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
    return sortOrder != null
        ? new AttrPathComparator<T_ITEM>(this, (InMemSortOrder)sortOrder)
        : null;
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

  @Override
  protected InMemExprEvaluator getExprEvaluator(FilterExpression expr) {
    return (InMemExprEvaluator) super.getExprEvaluator(expr);
  }

  @Override
  protected InMemCompOpEvaluator getCompOpEvaluator(FilterCompare compareOperation) {
    return (InMemCompOpEvaluator) super.getCompOpEvaluator(compareOperation);
  }

  /**
   * Gets the specified attribute value from the item.<br>
   * Sub classes may define here other value resolution algorithms.
   */
  public Object getAttrValue(Object item, QueryAttr attr) {
    if (attr instanceof QueryAttrMulti) {
      QueryAttrMulti mattr = (QueryAttrMulti) attr;

      List<QueryAttr> partAttrDefs = mattr.getParts();
      Object[] values = new Object[partAttrDefs.size()];
      for (int i=0; i<partAttrDefs.size(); ++i) {
        values[i] = getAttrValue(item, partAttrDefs.get(i));
      }
      return new MultiObjectValue(values);
    }
    else  {
      // XXX olaf: is called very often in case of long lists. Cache the parsed expressions!
      String path = attr.getPath();
      Expression expr = PathExpressionChain.parse(new ParseCtxt(path));
      Object value = expr.exec(new ExprExecCtxt(item));
      return value;
    }
  }


  /**
   * A comparator that allows to compare based on an {@link InMemQueryEvaluator} an {@link InMemSortOrder}.
   * <p>
   * The {@link InMemQueryEvaluator} provides the attribute values to compare.
   * <p>
   * TODO olaf: Add a second version that just works with a {@link SortOrder}.
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
      QueryAttr d = sortOrder.getAttr();
      Object v1 = evaluatorCtxt.getAttrValue(o1, d);
      Object v2 = evaluatorCtxt.getAttrValue(o2, d);

      return sortOrder.getComparator().compare(v1, v2);
    }

  }

}
