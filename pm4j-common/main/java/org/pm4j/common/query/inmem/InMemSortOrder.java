package org.pm4j.common.query.inmem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
public class InMemSortOrder implements SortOrder {
  private static final long        serialVersionUID = 1L;

  private final Comparator<Object> comparator;
  private final AttrDefinition     attrDefinition;
  private boolean                  ascending = true;

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
    assert attrDefinition != null;
    assert comparator != null;

    this.attrDefinition = attrDefinition;
    this.comparator = (Comparator<Object>) comparator;
  }

  public InMemSortOrder(Comparator<?> comparator) {
    this(new AttrDefinition("this", Object.class), comparator);
  }

  @Override
  public List<AttrSortSpec> getAttrSortSpecs() {
    return Arrays.asList(new AttrSortSpec(attrDefinition, ascending));
  }

  public Comparator<Object> getComparator() {
    return comparator;
  }

  @Override
  public SortOrder getReverseSortOrder() {
    InMemSortOrder so = new InMemSortOrder(attrDefinition, new InvertingComparator<Object>(comparator));
    so.ascending = !this.ascending;
    return so;
  }

  public AttrDefinition getAttrDefinition() {
    return attrDefinition;
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
