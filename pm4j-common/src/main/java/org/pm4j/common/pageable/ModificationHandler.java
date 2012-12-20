package org.pm4j.common.pageable;

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
public interface ModificationHandler<T_ITEM> {

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
   * @return <code>true</code> if there are removed items registered.
   */
//  boolean hasRemovedItems();

}
