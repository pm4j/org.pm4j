package org.pm4j.common.pageable;

import java.util.Collection;
import java.util.List;

import org.pm4j.common.selection.Selection;

/**
 * An interface that is used to provide reports about changed collection items.
 *
 * @author olaf boede
 *
 * @param <T_ITEM> type of handled collection items.
 */
public interface Modifications<T_ITEM> {

  /**
   * Reports if there is any modification registered.
   *
   * @return <code>true</code> if something is modified.
   */
  boolean isModified();

  /**
   * Provides the list of added items.<br>
   * The first added item appears as first list item.
   *
   * @return the added items.<br>
   *  If there is no added item: an empty collection.
   */
  List<T_ITEM> getAddedItems();

  /**
   * Provides the (unsorted) set of updated items.
   *
   *
   * @return the updated item set.<br>
   *  If there is no updated item: an empty collection.
   */
  Collection<T_ITEM> getUpdatedItems();

  /**
   * Provides the set of removed items.
   * <p>
   * Deleted items are reported as a {@link Selection} because a delete operation
   * may be applied on a selection of thousands of items.<br>
   * The application should not be forced to load all these items.
   *
   * @return the deleted items.<br>
   *  If there is no deleted item: an empty selection.
   */
  Selection<T_ITEM> getRemovedItems();

}
