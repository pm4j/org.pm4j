package org.pm4j.common.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
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

  /**
   * @param nextSortOrder the nextSortOrder to set
   * @deprecated Please never use it in domain code. SortOrders should be immutable.
   */
  // TODO: should be finally package local. Same applies for ascending.
  protected final void setNextSortOrder(SortOrder nextSortOrder) {
    this.nextSortOrder = nextSortOrder;
  }

  /**
   * @param ascending the ascending to set
   */
  protected final void setAscending(boolean ascending) {
    this.ascending = ascending;
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

  /**
   * Checks if both sort orders use the same chain of attributes.<br>
   * It ignores any asc/desc information.
   *
   * @param so1
   * @param so2
   * @return <code>true</code> if both use the same attribute set.
   */
  public static boolean bothOrdersUseTheSameAttributeSet(SortOrder so1, SortOrder so2) {
    if (so1 == so2) {
      return true;
    }
    if (so1 == null || so2 == null) {
      return false;
    }
    return ObjectUtils.equals(so1.getAttr(), so2.getAttr())
        && bothOrdersUseTheSameAttributeSet(so1.getNextSortOrder(), so2.getNextSortOrder());
  }

  /**
   * Joins a set of {@link SortOrder}s to a new single {@link SortOrder}.
   * 
   * The new {@link SortOrder} represents the array items using the {@link #nextSortOrder} chain.
   * 
   * @param sortOrders A set of sort orders to be joined. 
   * @return A single {@link SortOrder} or <code>null</code> if the given array was empty.
   */
  public static SortOrder join(SortOrder... sortOrders) {
    if (sortOrders.length == 0) {
      return null;
    }
    if (sortOrders.length == 1) {
      return sortOrders[0];
    }
    List<SortOrder> list = new ArrayList<>(sortOrders.length);
    for (SortOrder so : sortOrders) {
      addChainToList(list, so);
    }
    for (int i = 0; i<list.size()-1; ++i) {
      list.get(i).setNextSortOrder(list.get(i+1));
    }
    return list.get(0);
  }
  
  private static List<SortOrder> addChainToList(List<SortOrder> list, SortOrder so) {
    // Clone is needed because we need to support sub classes and are not allowed to
    // modify the original.
    SortOrder clone = so.clone();
    // Prevent double chains. Next SO will be considered as next list member.
    clone.setNextSortOrder(null);
    list.add(clone);
    
    if (so.getNextSortOrder() != null) {
      addChainToList(list, so.getNextSortOrder());
    }
    return list;
  }
  
}