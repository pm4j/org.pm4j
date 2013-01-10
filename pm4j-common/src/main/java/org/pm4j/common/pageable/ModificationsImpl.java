package org.pm4j.common.pageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.selection.EmptySelection;
import org.pm4j.common.selection.Selection;

/**
 * A basic {@link Modifications} implementation that supports add- and update change handling.
 *
 * @author olaf boede
 *
 * @param <T_ITEM> type of collection items.
 */
public class ModificationsImpl<T_ITEM> implements Modifications<T_ITEM> {

  @SuppressWarnings("unchecked")
  private Collection<T_ITEM> addedItems = Collections.EMPTY_LIST;

  @SuppressWarnings("unchecked")
  private Collection<T_ITEM> updatedItems = Collections.EMPTY_LIST;

  @SuppressWarnings("unchecked")
  private Selection<T_ITEM> removedItems = (Selection<T_ITEM>) EmptySelection.EMPTY_OBJECT_SELECTION;

  @Override
  public boolean isModified() {
    return !getAddedItems().isEmpty() || !getUpdatedItems().isEmpty() || getRemovedItems().getSize() > 0;
  }

  @Override
  public Collection<T_ITEM> getAddedItems() {
    return addedItems;
  }

  @Override
  public Collection<T_ITEM> getUpdatedItems() {
    return updatedItems;
  }

  @Override
  public Selection<T_ITEM> getRemovedItems() {
    return removedItems;
  }

  public void registerAddedItem(T_ITEM item) {
    if (addedItems.isEmpty()) {
      addedItems = new ArrayList<T_ITEM>();
    }
    addedItems.add(item);
  }

  public void registerUpdatedItem(T_ITEM item) {
    if (updatedItems.isEmpty()) {
      updatedItems = new ArrayList<T_ITEM>();
    }
    updatedItems.add(item);
  }

  public void setRemovedItems(Selection<T_ITEM> removedItems) {
    this.removedItems = removedItems;
    removeSelectedItemsFromCollection(removedItems, addedItems);
    removeSelectedItemsFromCollection(removedItems, updatedItems);
  }

  /** Removes the selected items without iterating the selection.
   * (selection iteration is potentially very slow) */
  private void removeSelectedItemsFromCollection(Selection<T_ITEM> removedItems, Collection<T_ITEM> collection) {
    List<T_ITEM> itemsToRemoveFromCollection = new ArrayList<T_ITEM>();
    for (T_ITEM i : collection) {
      if (removedItems.isSelected(i)) {
        itemsToRemoveFromCollection.add(i);
      }
    }
    if (!itemsToRemoveFromCollection.isEmpty()) {
      collection.removeAll(itemsToRemoveFromCollection);
    }
  }

}
