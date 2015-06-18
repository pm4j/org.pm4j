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
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.selection.SelectionWithAdditionalItems;

/**
 * Modification handler for query service based collections.
 *
 * @author Olaf Boede
 *
 * @param <T_ITEM> Type of collection items.
 * @param <T_ID> Item identifier type.
 */
public abstract class QueryCollectionModificationHandlerBase<T_ITEM, T_ID, T_SERVICE extends QueryService<T_ITEM, T_ID>>  implements ModificationHandler<T_ITEM> {

  private ModificationsImpl<T_ITEM> modifications = new ModificationsImpl<T_ITEM>();
  private final PageableCollectionBase<T_ITEM> pageableCollection;
  /** The service that provides the data to handle. */
  private final T_SERVICE service;

  public QueryCollectionModificationHandlerBase(PageableCollectionBase<T_ITEM> pageableCollection, T_SERVICE service) {
    assert pageableCollection != null;
    assert service != null;
    this.pageableCollection = pageableCollection;
    this.service = service;
  }

  protected final T_SERVICE getService() {
    return service;
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

    setRemovedItemsImpl(persistentRemovedItemSelection);

    pageableCollection.clearCaches();
    pageableCollection.firePropertyChange(PageableCollection.EVENT_REMOVE_SELECTION, selectedItems, null);
    return true;
  }

  @Override
  public void registerRemovedItems(Iterable<T_ITEM> items) {
    throw new UnsupportedOperationException("registerRemovedItems() is not yet implemented for query based collections.");
  }

  protected abstract QueryExpr createRemovedItemsExpr(QueryExpr queryFilterExpr);

  protected abstract void setRemovedItemsImpl(Selection<T_ITEM> persistentRemovedItemSelection);

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

  protected ModificationsImpl<T_ITEM> getModificationsImpl() {
    return modifications;
  }

  /**
   * @return the pageable collection
   */
  protected final PageableCollectionBase<T_ITEM> getPageableCollection() {
    return pageableCollection;
  }

}
