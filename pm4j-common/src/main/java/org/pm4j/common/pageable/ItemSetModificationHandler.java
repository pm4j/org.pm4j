package org.pm4j.common.pageable;

import java.util.Collection;

import org.pm4j.common.selection.Selection;

/**
 * Handles add and delete-modifications for a {@link PageableCollection2}.
 * <p>
 * The collection needs to be informed about (transient) collection changes
 * to consider them.
 * <p>
 * This handler also provides information about these changes.
 *
 * @author olaf boede
 */
public interface ItemSetModificationHandler<T_ITEM> {

  /**
   * Adds an item to this collection.
   * <p>
   * Usually the new item will be added as the last collection item.
   *
   * @param item the item to add.
   * @throws UnsupportedOperationException if the collection does not support additional items.
   */
  void addItem(T_ITEM item);

  /**
   * Removes these items from the collection.
   *
   * @param items the items to remove.
   * @throws UnsupportedOperationException if the collection does not support removed item handling.
   */
  void removeItems(Selection<T_ITEM> items);

  /**
   * Reports if there is any modification registered.
   *
   * @return <code>true</code> if something is modified.
   */
  boolean isModified();

  /**
   * Provides the set of added items.
   *
   * @return the added items.<br>
   *  If there is no added item: an empty collection.
   */
  Collection<T_ITEM> getAddedItems();

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

  /**
   * Clears the registerd item set change information.<br>
   * This gets usually called after saving the modifications.
   * <p>
   * It does <b>not</b> undo the changes!
   */
  void clearRegisteredModifications();
}
