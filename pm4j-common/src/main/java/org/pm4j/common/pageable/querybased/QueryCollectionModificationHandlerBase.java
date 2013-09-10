package org.pm4j.common.pageable.querybased;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.pageable.ModificationHandler;
import org.pm4j.common.pageable.Modifications;
import org.pm4j.common.pageable.ModificationsImpl;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.pageable.querybased.PageableQuerySelectionHandler.ItemIdSelection;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterNot;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.selection.ItemIdConverter;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.selection.SelectionWithAdditionalItems;

public class QueryCollectionModificationHandlerBase<T_ITEM, T_ID>  implements ModificationHandler<T_ITEM> {

  private ModificationsImpl<T_ITEM> modifications = new ModificationsImpl<T_ITEM>();
  private final PageableCollectionBase2<T_ITEM> pageableCollection;
  /** The service that provides the data to handle. */
  private ItemIdConverter<T_ITEM, T_ID> service;

  public QueryCollectionModificationHandlerBase(PageableCollectionBase2<T_ITEM> pageableCollection, ItemIdConverter<T_ITEM, T_ID> service) {
    assert pageableCollection != null;
    assert service != null;
    this.pageableCollection = pageableCollection;
    this.service = service;
  }

  public FilterExpression getRemovedItemsFilterExpr(FilterExpression queryFilterExpr) {
    @SuppressWarnings("unchecked")
    ClickedIds<T_ID> ids = modifications.getRemovedItems().isEmpty()
        ? new ClickedIds<T_ID>()
        : ((ItemIdSelection<T_ITEM, T_ID>)modifications.getRemovedItems()).getClickedIds();
    QueryAttr idAttr = pageableCollection.getQueryOptions().getIdAttribute();
    return new FilterNot(PageableCollectionUtil2.makeSelectionQueryParams(idAttr, queryFilterExpr, ids));
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean removeSelectedItems() {
    // Nothing to remove in case of an empty selection.
    Selection<T_ITEM> selectedItems = pageableCollection.getSelection();
    if (selectedItems.isEmpty()) {
      return true;
    }

    // Check for vetos
    try {
      pageableCollection.fireVetoableChange(PageableCollection2.EVENT_REMOVE_SELECTION, selectedItems, null);
    } catch (PropertyVetoException e) {
      return false;
    }

    // Clear the selection. All selected items will be deleted.
    SelectionHandlerUtil.forceSelectAll(pageableCollection.getSelectionHandler(), false);

    // Identify the sets of persistent and transient items to delete.
    Selection<T_ITEM> persistentItems = selectedItems;

    // In case of a selection with transient items, we have to handle transient items accordingly.
    if (selectedItems instanceof SelectionWithAdditionalItems) {
      // Removed transient items will simply be forgotten.
      List<T_ITEM> transientItems = ((SelectionWithAdditionalItems<T_ITEM>)selectedItems).getAdditionalSelectedItems();
      for (T_ITEM i : new ArrayList<T_ITEM>(transientItems)) {
        modifications.unregisterAddedItem(i);
      }

      // Only the persistent sub-set needs to be handled in the following code.
      persistentItems = ((SelectionWithAdditionalItems<T_ITEM>)selectedItems).getBaseSelection();
    }

    // Remember the previous set of removed items. It needs to be extended by some additional items to remove.
    Selection<T_ITEM> oldRemovedItemSelection = modifications.getRemovedItems();


    if (persistentItems instanceof ItemIdSelection) {
      if (oldRemovedItemSelection.isEmpty()) {
        modifications.setRemovedItems((ItemIdSelection<T_ITEM, T_ID>) persistentItems);
      } else {
        Collection<T_ID> ids = ((ItemIdSelection<T_ITEM, T_ID>) persistentItems).getClickedIds().getIds();
        // XXX olaf: assumes that we handle removed items as ItemIdSelection. But that will change as soon
        // as we add remove handling for inverted selections.
        modifications.setRemovedItems(new ItemIdSelection<T_ITEM, T_ID>((ItemIdSelection<T_ITEM, T_ID>)oldRemovedItemSelection, ids));
      }
    } else {
   // TODO olaf: inverted selections are not yet handled by query
//    if (items instanceof PageableQuerySelectionHandler.InvertedSelection) {
//      removedInvertedItemSelections.addSelection(items);
      // TODO: add exception interface signature.
      long newSize = persistentItems.getSize() + oldRemovedItemSelection.getSize();
      if (newSize > 1000) {
        throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
      }

      Collection<T_ID> ids = PageableQueryUtil.getItemIds(service, persistentItems);
      modifications.setRemovedItems(oldRemovedItemSelection.isEmpty()
          // XXX oboede: here was the cached internal service used. But i suspect that the external service isn't less
          // efficient.
          ? new ItemIdSelection<T_ITEM, T_ID>(service, ids)
          : new ItemIdSelection<T_ITEM, T_ID>((ItemIdSelection<T_ITEM, T_ID>)oldRemovedItemSelection, ids));
    }

    pageableCollection.clearCaches();
    pageableCollection.firePropertyChange(PageableCollection2.EVENT_REMOVE_SELECTION, selectedItems, null);
    return true;
  }

  @Override
  public void addItem(T_ITEM item) {
    modifications.registerAddedItem(item);
    pageableCollection.firePropertyChange(PageableCollection2.EVENT_ITEM_ADD, null, item);
  }

  @Override
  public void updateItem(T_ITEM item, boolean isUpdated) {
    // a modification of a new item should not lead to a double-listing within the updated list too.
    if (isUpdated && modifications.getAddedItems().contains(item)) {
      return;
    }

    boolean wasUpdated = modifications.getUpdatedItems().contains(item);
    modifications.registerUpdatedItem(item, isUpdated);
    pageableCollection.firePropertyChange(PageableCollection2.EVENT_ITEM_UPDATE, wasUpdated, isUpdated);
  };

  @Override
  public void clearRegisteredModifications() {
    modifications = new ModificationsImpl<T_ITEM>();
  }

  @Override
  public Modifications<T_ITEM> getModifications() {
    return modifications;
  }

}

