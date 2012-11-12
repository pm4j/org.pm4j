package org.pm4j.common.query;

import java.io.Serializable;

import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * A sort order specification.
 *
 * @author olaf boede
 */
public class SortOrder implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  private boolean ascending = true;
  private final AttrDefinition attribute;
  private final SortOrder nextSortOrder;

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

  @Override
  public SortOrder clone() {
    try {
      return (SortOrder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  @Override
  public String toString() {
    return attribute.getPathName() + " " + (ascending ? "asc" : "desc");
  }

  public SortOrder getNextSortOrder() {
    return nextSortOrder;
  }

}