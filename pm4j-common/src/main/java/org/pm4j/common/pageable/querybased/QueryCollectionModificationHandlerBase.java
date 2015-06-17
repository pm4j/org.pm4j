package org.pm4j.common.pageable.querybased;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.modifications.ModificationsImpl;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionBase;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.pageable.querybased.pagequery.ClickedIds;
import org.pm4j.common.pageable.querybased.pagequery.ItemIdSelection;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprNot;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.selection.SelectionWithAdditionalItems;
import org.pm4j.common.util.collection.ListUtil;

/**
 * Modification handler for query service based collections.
 *
 * @author Olaf Boede
 *
 * @param <T_ITEM> Type of collection items.
 * @param <T_ID> Item identifier type.
 */
public class QueryCollectionModificationHandlerBase<T_ITEM, T_ID>  implements ModificationHandler<T_ITEM> {

  private ModificationsImpl<T_ITEM> modifications = new ModificationsImpl<T_ITEM>();
  private final PageableCollectionBase<T_ITEM> pageableCollection;
  /** The service that provides the data to handle. */
  private final QueryService<T_ITEM, T_ID> service;

  public QueryCollectionModificationHandlerBase(PageableCollectionBase<T_ITEM> pageableCollection, QueryService<T_ITEM, T_ID> service) {
    assert pageableCollection != null;
    assert service != null;
    this.pageableCollection = pageableCollection;
    this.service = service;
  }

  public QueryExpr getRemovedItemsFilterExpr(QueryExpr queryFilterExpr) {
    @SuppressWarnings("unchecked")
    ClickedIds<T_ID> ids = modifications.getRemovedItems().isEmpty()
        ? new ClickedIds<T_ID>()
        : ((ItemIdSelection<T_ITEM, T_ID>)modifications.getRemovedItems()).getClickedIds();
    QueryAttr idAttr = pageableCollection.getQueryOptions().getIdAttribute();
    return new QueryExprNot(PageableCollectionUtil.makeSelectionQueryParams(idAttr, queryFilterExpr, ids));
  }

  @Override
  public boolean removeSelectedItems() {
    // Nothing to remove in case of an empty selection.
    Selection<T_ITEM> selectedItems = pageableCollection.getSelection();
    if (selectedItems.isEmpty()) {
      return true;
    }

    // Check for vetos
    try {
      pageableCollection.fireVetoableChange(PageableCollection.EVENT_REMOVE_SELECTION, selectedItems, null);
    } catch (PropertyVetoException e) {
      return false;
    }

    // Clear the selection. All selected items will be deleted.
    SelectionHandlerUtil.forceSelectAll(pageableCollection.getSelectionHandler(), false);

    // Identify the sets of persistent and transient items to delete.
    Selection<T_ITEM> persistentRemovedItemSelection = selectedItems;

    // In case of a selection with transient items, we have to handle transient items accordingly.
    if (selectedItems instanceof SelectionWithAdditionalItems) {
      // Removed transient items will simply be forgotten.
      List<T_ITEM> transientItems = ((SelectionWithAdditionalItems<T_ITEM>)selectedItems).getAdditionalSelectedItems();
      for (T_ITEM i : new ArrayList<T_ITEM>(transientItems)) {
        modifications.unregisterAddedItem(i);
      }

      // Only the persistent sub-set needs to be handled in the following code.
      persistentRemovedItemSelection = ((SelectionWithAdditionalItems<T_ITEM>)selectedItems).getBaseSelection();
    }

    registerRemovedItems(modifications, persistentRemovedItemSelection);

    pageableCollection.clearCaches();
    pageableCollection.firePropertyChange(PageableCollection.EVENT_REMOVE_SELECTION, selectedItems, null);
    return true;
  }

  // TODO
  protected void registerRemovedItems(ModificationsImpl<T_ITEM> modifications, Selection<T_ITEM> persistentRemovedItemSelection) {
    // Remember the previous set of removed items. It needs to be extended by some additional items to remove.
    Selection<T_ITEM> oldRemovedItemSelection = modifications.getRemovedItems();
    // XXX oboede: currently ItemIdSelection is an internal precondition
    if (oldRemovedItemSelection.isEmpty() && persistentRemovedItemSelection instanceof ItemIdSelection)
    {
      modifications.setRemovedItems(persistentRemovedItemSelection);
    } else {
      if (! (persistentRemovedItemSelection instanceof ItemIdSelection)) {
        // TODO olaf: big inverted selections are not yet supported
        long newSize = persistentRemovedItemSelection.getSize() + oldRemovedItemSelection.getSize();
        if (newSize > 1000) {
          throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
        }
      }

      Collection<T_ID> ids = ListUtil.collectionsToList(getItemIds(persistentRemovedItemSelection), getItemIds(oldRemovedItemSelection));
      modifications.setRemovedItems(createItemIdSelection(ids));
    }
  }

  /**
   * Can be overridden if the concrete collection needs a special strategy.
   *
   * @param queryService
   * @param ids
   * @return a newly created ItemIdSelection
   */
  protected ItemIdSelection<T_ITEM, T_ID> createItemIdSelection(Collection<T_ID> ids) {
    return new ItemIdSelection<T_ITEM, T_ID>(service, ids);
  }

  @Override
  public void registerRemovedItems(Iterable<T_ITEM> items) {
    throw new UnsupportedOperationException("registerRemovedItems() is not yet implemented for query based collections.");
  }

  /**
   * Add and register add are the same for query collections.<br>
   * The set of added items within the {@link Modifications} is the transient
   * item storage until the modifications will be persisted at some point.
   */
  @Override
  public boolean addItem(T_ITEM item) {
    // check for vetos before doing the change
    try {
      pageableCollection.fireVetoableChange(PageableCollection.EVENT_ITEM_ADD, null, item);
    } catch (PropertyVetoException e) {
      return false;
    }

    registerAddedItem(item);
    return true;
  }

  @Override
  public void registerAddedItem(T_ITEM item) {
    modifications.registerAddedItem(item);
    pageableCollection.firePropertyChange(PageableCollection.EVENT_ITEM_ADD, null, item);
  }

  @Override
  public void registerUpdatedItem(T_ITEM item, boolean isUpdated) {
    // a modification of a new item should not lead to a double-listing within the updated list too.
    if (isUpdated && modifications.getAddedItems().contains(item)) {
      return;
    }

    boolean wasUpdated = modifications.getUpdatedItems().contains(item);
    if (wasUpdated != isUpdated) {
      modifications.registerUpdatedItem(item, isUpdated);
      pageableCollection.firePropertyChange(PageableCollection.EVENT_ITEM_UPDATE, wasUpdated, isUpdated);
    }
  };

  @Override
  public void clear() {
    modifications = new ModificationsImpl<T_ITEM>();
  }

  @Override
  public Modifications<T_ITEM> getModifications() {
    return modifications;
  }

  @Override
  public void setModifications(Modifications<T_ITEM> modifications) {
    assert modifications != null;
    this.modifications = (ModificationsImpl<T_ITEM>) modifications;
  }

  @SuppressWarnings("unchecked")
  private Collection<T_ID> getItemIds(Selection<T_ITEM> selection) {
    if (selection instanceof ItemIdSelection) {
      return ((ItemIdSelection<T_ITEM, T_ID>)selection).getClickedIds().getIds();
    } else {
      Collection<T_ID> ids = new ArrayList<T_ID>((int)selection.getSize());
      for (T_ITEM i : selection) {
        ids.add(service.getIdForItem(i));
      }
      return ids;
    }
  }
}

