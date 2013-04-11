package org.pm4j.common.query;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * A sort order specification.
 *
 * @author olaf boede
 */
public class SortOrder implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  private boolean ascending;
  private final QueryAttr attr;
  private SortOrder nextSortOrder;

  /**
   * Creates a sort order for a single attribute in ascending order.
   *
   * @param attribute
   *            definition of the attribute to sort on
   */
  public SortOrder(final QueryAttr attribute) {
    this(attribute, true, null);
  }

  /**
   * Creates a sort order for a single attribute.
   *
   * @param attr
   *            definition of the attribute to sort on
   * @param ascending
   *            true if order is ascending values
   */
  public SortOrder(final QueryAttr attr, final boolean ascending) {
    this(attr, ascending, null);
  }

  /**
   * Creates a sort order that sorts by multiple attributes.
   *
   * @param attr
   *            definition of the attribute to sort on
   * @param ascending
   *            true if order is ascending values
   * @param nextSortOrder the next attribute to sort by.
   */
  public SortOrder(final QueryAttr attr, final boolean ascending, SortOrder nextSortOrder) {
    assert attr != null;

    this.attr = attr;
    this.ascending = ascending;
    this.nextSortOrder = nextSortOrder;
  }

  /**
   * Creates an ascending sort order for multiple attributes.
   *
   * @param attrs the set of attributes to sort by. The first one is the most significant.
   */
  public SortOrder(final QueryAttr... attrs) {
    assert attrs.length > 0;

    this.attr = attrs[0];
    this.ascending = true;

    SortOrder so = this;
    for (int i=1; i<attrs.length; ++i) {
      so.nextSortOrder = new SortOrder(attrs[i]);
      so = so.nextSortOrder;
    }
  }

  /**
   * @return the ascending
   */
  public boolean isAscending() {
      return ascending;
  }

  /**
   * @return the attribute
   */
  public QueryAttr getAttr() {
      return attr;
  }

  /**
   * Provides a copy of this sort order that sorts in the reverse order.
   *
   * @return A reverse sort order definition.
   */
  public SortOrder getReverseSortOrder() {
    SortOrder reverseOrder = clone();
    SortOrder s = reverseOrder;
    while (s != null) {
      s.ascending = !s.ascending;
      s = s.nextSortOrder;
    }
    return reverseOrder;
  }

  public SortOrder getNextSortOrder() {
    return nextSortOrder;
  }

  @Override
  public SortOrder clone() {
    try {
      return (SortOrder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (! (obj instanceof SortOrder)) {
      return false;
    }
    SortOrder other = (SortOrder)obj;
    return new EqualsBuilder().append(attr, other.attr).append(ascending, other.ascending).append(nextSortOrder, other.nextSortOrder).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(23, 47).append(attr).append(ascending).append(nextSortOrder).toHashCode();
  }

  @Override
  public String toString() {
    return attr + " " + (ascending ? "asc" : "desc");
  }

}