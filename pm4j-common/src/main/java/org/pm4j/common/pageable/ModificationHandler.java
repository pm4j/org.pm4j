package org.pm4j.common.pageable;

import java.beans.PropertyChangeEvent;

import org.pm4j.common.pageable.querybased.PageableQueryService;


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
   * <p>
   * Fires a {@link PropertyChangeEvent} {@link PageableCollection2#EVENT_ITEM_ADD}.
   *
   * @param item the item to add.
   * @throws UnsupportedOperationException if the collection does not support additional items.
   */
  void addItem(T_ITEM item);

  /**
   * Registers the passed item as an updated item.
   * <p>
   * Fires a {@link PropertyChangeEvent} {@link PageableCollection2#EVENT_ITEM_UPDATE}.
   *
   * @param the
   *          updated item. It should be part of the collection.
   * @param isUpdated
   *          indicates if the item should be added or removed from the set of
   *          updated items.
   */
  void updateItem(T_ITEM item, boolean isUpdated);

  /**
   * Removes all currently selected items from the collection.
   * <p>
   * Fires a {@link PropertyChangeEvent} {@link PageableCollection2#EVENT_REMOVE_SELECTION}.
   * <p>
   * The following behavior is implemented in relation to specific pageable collections:<br>
   * In case of an in-memory collection, the delete operation will be applied immediately to the backing collection.<br>
   * In case of {@link PageableQueryService} based collection the delete operation will just mark the selected items
   * as deleted within the {@link Modifications} object. All deleted items will no longer be retrieved.
   *
   * @return <code>false</code> if the de-selection of the items to delete was prevented by a selection change decorator.
   * @throws UnsupportedOperationException if the collection does not support removed item handling.
   */
  boolean removeSelectedItems();

  /**
   * Clears the registered item set change information.<br>
   * This gets usually called after saving the modifications.
   * <p>
   * It does <b>not</b> undo the changes!
   */
  void clearRegisteredModifications();

  /**
   * Provides the registered modifications.
   *
   * @return A container for the registered modifications. Never <code>null</code>.
   */
  Modifications<T_ITEM> getModifications();

}
