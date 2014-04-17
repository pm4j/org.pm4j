package org.pm4j.common.modifications;

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

  /** The ordered set of (manually) added items. Ordered according to the sequence of {@link #registerAddedItem(Object)} calls. */
  private List<T_ITEM> addedItems = Collections.emptyList();

  /** The set of (manually) updated items. Not sorted. */
  private Collection<T_ITEM> updatedItems = Collections.emptyList();

  /** Removed items are handled as a {@link Selection} to be able to support large selections. */
  @SuppressWarnings("unchecked")
  private Selection<T_ITEM> removedItems = (Selection<T_ITEM>) EmptySelection.EMPTY_OBJECT_SELECTION;

  @Override
  public boolean isModified() {
    return !getAddedItems().isEmpty() || !getUpdatedItems().isEmpty() || getRemovedItems().getSize() > 0;
  }

  @Override
  public List<T_ITEM> getAddedItems() {
    return addedItems;
  }

  /** A pm4j internal functionality that's public only for technical reasons. Please don't call it! */
  public void registerAddedItem(T_ITEM item) {
    if (addedItems.isEmpty()) {
      addedItems = new ArrayList<T_ITEM>();
    }
    if (!addedItems.contains(item)) {
      addedItems.add(item);
    }
  }

  /**
   * A pm4j internal functionality that's public only for technical reasons. Please don't call it!
   * <p>
   * Removes the given item from the list of added items explicitely.
   *
   * @param item the item to remove.
   */
  public void unregisterAddedItem(T_ITEM item) {
    if (!addedItems.isEmpty()) {
      addedItems.remove(item);
    }
  }

  @Override
  public Collection<T_ITEM> getUpdatedItems() {
    return updatedItems;
  }

  /** A pm4j internal functionality that's public only for technical reasons. Please don't call it! */
  public void registerUpdatedItem(T_ITEM item, boolean isUpdated) {
    if (isUpdated) {
      if (updatedItems.isEmpty()) {
        updatedItems = new ArrayList<T_ITEM>();
      }
      if (!updatedItems.contains(item)) {
        updatedItems.add(item);
      }
    }
    else {
      if (!updatedItems.isEmpty()) {
        updatedItems.remove(item);
      }
    }
  }

  @Override
  public Selection<T_ITEM> getRemovedItems() {
    return removedItems;
  }

  /** A pm4j internal functionality that's public only for technical reasons. Please don't call it! */
  public void setRemovedItems(Selection<T_ITEM> removedItems) {
    this.removedItems = removedItems;
    // XXX olaf: this line may disappear because the calling code should organize
    // the added items...
    removeSelectedItemsFromCollection(removedItems, addedItems);
    removeSelectedItemsFromCollection(removedItems, updatedItems);
  }

  /** Removes the selected items without iterating the selection.
   * (selection iteration is potentially very slow) */
  private void removeSelectedItemsFromCollection(Selection<T_ITEM> removedItems, Collection<T_ITEM> collection) {
    List<T_ITEM> itemsToRemoveFromCollection = new ArrayList<T_ITEM>();
    for (T_ITEM i : collection) {
      if (removedItems.contains(i)) {
        itemsToRemoveFromCollection.add(i);
      }
    }
    if (!itemsToRemoveFromCollection.isEmpty()) {
      collection.removeAll(itemsToRemoveFromCollection);
    }
  }

  @Override
  public String toString() {
    if (!isModified()) {
      return "Modifications{}";
    } else {
      StringBuilder sb = new StringBuilder("Modifications{");
      sb.append("added: " + addedItems.size());
      sb.append(", updated: " + updatedItems.size());
      sb.append(", removed: " + removedItems.getSize());
      sb.append("}");
      return sb.toString();
    }
  }

}
