package org.pm4j.common.query.inmem;

import java.io.Serializable;
import java.util.Comparator;

import org.pm4j.common.query.AttrDefinition;
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

  public InMemSortOrder(AttrDefinition attrDefinition) {
    this(attrDefinition, new ComparableComparator());
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
  public InMemSortOrder(AttrDefinition attrDefinition, Comparator<?> comparator) {
    super(attrDefinition, true);
    this.comparator = (Comparator<Object>) comparator;
  }

  public InMemSortOrder(Comparator<?> comparator) {
    this(new AttrDefinition("this", Object.class), comparator);
  }

  public Comparator<Object> getComparator() {
    return comparator;
  }

  @Override
  public SortOrder getReverseSortOrder() {
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
