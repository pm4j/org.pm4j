package org.pm4j.common.query.inmem;

import java.io.Serializable;
import java.util.Comparator;

import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.common.util.InvertingComparator;

/**
 * A sort order definition that provides an item comparator for an in-memory
 * based sort operation.
 *
 * @author olaf boede
 */
public class InMemSortOrder extends SortOrder {
  private static final long        serialVersionUID = 1L;

  private Comparator<Object> comparator;

  public InMemSortOrder(QueryAttr attrDefinition) {
    this(attrDefinition, new ComparableComparator());
  }

  /**
   * Generates an in-memory sort order based on a technology neutral sort order
   * definition.<br>
   * Generates the complete sort order chain as found in the given base sort order.
   *
   * @param baseSortOrder The sort order to use.
   */
  public InMemSortOrder(SortOrder baseSortOrder) {
    this(baseSortOrder.getAttr());
    // handle inverse sort order definitions
    if (!baseSortOrder.isAscending()) {
      comparator = new InvertingComparator<Object>(comparator);
      setAscending(false);
    }
    // add all chained sort order attributes.
    if (baseSortOrder.getNextSortOrder() != null) {
      setNextSortOrder(new InMemSortOrder(baseSortOrder.getNextSortOrder()));
    }
  }

  /**
   *
   * @param attrDefinition
   * @param comparator
   *          the internally used comparator.<br>
   *          Should be {@link Serializable} if a related selection of a query
   *          based collection needs to be serialized.
   */
  @SuppressWarnings("unchecked")
  public InMemSortOrder(QueryAttr attrDefinition, Comparator<?> comparator) {
    super(attrDefinition, true);
    this.comparator = (Comparator<Object>) comparator;
  }

  public InMemSortOrder(Comparator<?> comparator) {
    this(new QueryAttr("this", Object.class), comparator);
  }

  public Comparator<Object> getComparator() {
    return comparator;
  }

  @Override
  public InMemSortOrder getReverseSortOrder() {
    InMemSortOrder reverse = (InMemSortOrder) super.getReverseSortOrder();
    if (comparator.getClass() == InvertingComparator.class) {
      reverse.comparator = ((InvertingComparator<Object>)comparator).getBaseComparator();
    } else {
      reverse.comparator = new InvertingComparator<Object>(comparator);
    }

    return reverse;
  }

  static class ComparableComparator implements Comparator<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Object v1, Object v2) {
      assert v1 == null || v1 instanceof Comparable;
      assert v2 == null || v2 instanceof Comparable;

      return CompareUtil.compare((Comparable<?>) v1, (Comparable<?>) v2);
    }
  }

}
