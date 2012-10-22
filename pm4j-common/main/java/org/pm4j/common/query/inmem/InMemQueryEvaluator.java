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
import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.EvaluatorSet;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.QueryEvaluatorBase;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.util.collection.ListUtil;

public class InMemQueryEvaluator<T_ITEM> extends QueryEvaluatorBase {

  public InMemQueryEvaluator() {
    super(InMemEvaluatorSet.INSTANCE);
  }

  public InMemQueryEvaluator(EvaluatorSet evaluatorSet) {
    super(evaluatorSet);
  }

  public boolean evaluate(T_ITEM item, FilterExpression expr) {
    InMemExprEvaluator<T_ITEM> ev = getExprEvaluator(expr);
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

    InMemExprEvaluator<T_ITEM> ev = getExprEvaluator(expr);
    List<T_ITEM> resultList = new ArrayList<T_ITEM>();
    for (T_ITEM i : items) {
      if (ev.eval(this, i, expr)) {
        resultList.add(i);
      }
    }

    return resultList;
  }

  public Comparator<T_ITEM> getComparator(SortOrder sortOrder) {
    return sortOrder != null
        ? new AttrPathComparator<T_ITEM>(this, (InMemSortOrder)sortOrder)
        : null;
  }

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

  @SuppressWarnings("unchecked")
  @Override
  protected InMemExprEvaluator<T_ITEM> getExprEvaluator(FilterExpression expr) {
    return (InMemExprEvaluator<T_ITEM>) super.getExprEvaluator(expr);
  }

  @Override
  protected InMemCompOpEvaluator getCompOpEvaluator(CompOp compOp) {
    return (InMemCompOpEvaluator) super.getCompOpEvaluator(compOp);
  }

  /**
   * Gets the specified attribute value from the item.
   */
  public Object getAttrValue(T_ITEM item, AttrDefinition attr) {
    // XXX olaf: is called very oftern in case of long lists. cache the parsed expressions
    Expression expr = PathExpressionChain.parse(new ParseCtxt(attr.getPathName()));
    Object value = expr.exec(new ExprExecCtxt(item));
    return value;
  }



  public static class AttrPathComparator<T> implements Comparator<T> {

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
      AttrDefinition d = sortOrder.getAttrDefinition();
      Object v1 = evaluatorCtxt.getAttrValue(o1, d);
      Object v2 = evaluatorCtxt.getAttrValue(o2, d);

      return sortOrder.getComparator().compare(v1, v2);
    }

  }

}
