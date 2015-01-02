package org.pm4j.core.pm;

import java.util.List;

import org.pm4j.core.pm.annotation.PmFactoryCfg;

/**
 * Provides an attribute that represents a list of beans as a list of PM's.
 * <p>
 * Needs a {@link PmFactoryCfg} to define the PM to be created for each bean list item.
 * <p>
 * Example: The set of childeren of a person object may be represented by
 * an element set with the name 'children'.
 *
 * @author olaf boede
 */
public interface PmAttrPmList<T_ITEM_PM extends PmBean<?>>
  extends PmAttr<List<T_ITEM_PM>> {

  void add(T_ITEM_PM pmElement);

  boolean remove(T_ITEM_PM pmElement);

  /**
   * An explicte list value signature. This signature is required for reflection
   * based frameworks that analyze the method signature for their data binding
   * (e.g. JSF). Such analysis for {@link PmAttr#getValue()} will just find out
   * that the method returns an object. The same analysis perfomed on this
   * method will provide the result that the returned value is a list...
   *
   * @deprecated please use {@link #getValue()}.
   * @return The list value.
   */
  List<T_ITEM_PM> getValueAsList();

  /**
   * Provides a subset of the whole item set.
   *
   * @param fromIdx
   *          Index of the first item to get.
   * @param numItems
   *          The maximal number of items to get.
   * @return The subset. May be empty but never <code>null</code>
   */
  List<T_ITEM_PM> getValueSubset(int fromIdx, int numItems);

  /**
   * @return The first list item. <code>null</code> in case of an empty list.
   */
  T_ITEM_PM getFirstItem();

  /**
   * @return The last list item. <code>null</code> in case of an empty list.
   */
  T_ITEM_PM getLastItem();

  /**
   * @see #getValueAsList()
   * @param value
   *          The new list value.
   * @deprecated please use {@link #setValue(Object)}.
   */
  void setValueAsList(List<T_ITEM_PM> value);

  /**
   * @return number of list items provided by #{link {@link #getValue()}.
   */
  int getSize();

  /**
   * For lists that may contain invisible items.
   * <p>
   * Used for very special JSF components (such as t:dataList) which require
   * at least a single dummy element to be able to be refreshed by ajax requests...
   *
   * @return <code>true</code> if at least a single list item is visible.
   */
  boolean getHasVisibleItems();
}
