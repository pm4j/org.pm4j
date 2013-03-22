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
  private final AttrDefinition attribute;
  private SortOrder nextSortOrder;

  /**
   * Creates a sort order for a single attribute in ascending order.
   *
   * @param attribute
   *            definition of the attribute to sort on
   */
  public SortOrder(final AttrDefinition attribute) {
    this(attribute, true, null);
  }

  /**
   * Creates a sort order for a single attribute.
   *
   * @param attribute
   *            definition of the attribute to sort on
   * @param ascending
   *            true if order is ascending values
   */
  public SortOrder(final AttrDefinition attribute, final boolean ascending) {
    this(attribute, ascending, null);
  }

  /**
   * Creates a sort order that sorts by multiple attributes.
   *
   * @param attribute
   *            definition of the attribute to sort on
   * @param ascending
   *            true if order is ascending values
   * @param nextSortOrder the next attribute to sort by.
   */
  public SortOrder(final AttrDefinition attribute, final boolean ascending, SortOrder nextSortOrder) {
    assert attribute != null;

    this.attribute = attribute;
    this.ascending = ascending;
    this.nextSortOrder = nextSortOrder;
  }

  /**
   * Creates an ascending sort order for multiple attributes.
   *
   * @param attributes the set of attributes to sort by. The first one is the most significant.
   */
  public SortOrder(final AttrDefinition... attributes) {
    assert attributes.length > 0;

    this.attribute = attributes[0];
    this.ascending = true;

    SortOrder so = this;
    for (int i=1; i<attributes.length; ++i) {
      so.nextSortOrder = new SortOrder(attributes[i]);
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
  public AttrDefinition getAttribute() {
      return attribute;
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
    return new EqualsBuilder().append(attribute, other.attribute).append(ascending, other.ascending).append(nextSortOrder, other.nextSortOrder).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(23, 47).append(attribute).append(ascending).append(nextSortOrder).toHashCode();
  }

  @Override
  public String toString() {
    return attribute.getPathName() + " " + (ascending ? "asc" : "desc");
  }

}