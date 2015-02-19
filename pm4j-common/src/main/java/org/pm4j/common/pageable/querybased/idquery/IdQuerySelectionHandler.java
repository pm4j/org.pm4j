package org.pm4j.common.pageable.querybased.idquery;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerBase;


/**
 * {@link SelectionHandler} for {@link IdQueryCollectionImpl}s.
 *
 * @param <T_ITEM> type of items to handle.
 * @param <T_ID> type of item id's.
 *
 * @author Olaf Boede
 */
public abstract class IdQuerySelectionHandler<T_ITEM, T_ID> extends SelectionHandlerBase<T_ITEM> {

  private static final Logger LOG = LoggerFactory.getLogger(IdQuerySelectionHandler.class);

  /** Used to provide empty selections. */
  private final IdQuerySelectionBase<T_ITEM, T_ID> emptySelection;

  /** The converter used to get items for the internally handled id's. */
  private final IdQueryService<T_ITEM, T_ID> idQueryService;

  /** The current selection. */
  private IdQuerySelectionBase<T_ITEM, T_ID> selection;

  @SuppressWarnings("unchecked")
  public IdQuerySelectionHandler(IdQueryService<T_ITEM, T_ID> idQueryService) {
    assert idQueryService != null;

    this.idQueryService = idQueryService;
    this.emptySelection = new IdQuerySelectionBase<T_ITEM, T_ID>(idQueryService, Collections.EMPTY_SET);
    this.selection = emptySelection;
  }

  /**
   * Sub classes should provide an implementation. Used for the 'selectAll' functionality.
   *
   * @return all item id's.
   */
  protected abstract Collection<T_ID> getAllIds();


   @Override
  public boolean select(boolean select, T_ITEM item) {
    Set<T_ID> set = getModifiableIdSet();
    T_ID id = idQueryService.getIdForItem(item);
    if (select) {
      beforeAddSingleItemSelection(set);
      set.add(id);
    }
    else {
      set.remove(id);
    }
    return setSelection(set);
  }

  @Override
  public boolean select(boolean select, Iterable<T_ITEM> items) {
    Set<T_ID> ids = getModifiableIdSet();

    if (getSelectMode() == SelectMode.SINGLE && select) {
      ids.clear();
    }

    for (T_ITEM i : items) {
      if (select) {
        ids.add(idQueryService.getIdForItem(i));
      }
      else {
        ids.remove(idQueryService.getIdForItem(i));
      }
    }

    checkMultiSelectResult(ids);
    return setSelection(ids);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean selectAll(boolean select) {
    if (select) {
      if (getSelectMode() != SelectMode.MULTI) {
        throw new RuntimeException("Select all for current select mode is not supported: " + getSelectMode());
      }

      return setSelection(new HashSet<T_ID>(getAllIds()));
    }
    else {
      return setSelection(Collections.EMPTY_SET);
    }
  }

  @Override
  public boolean invertSelection() {
    if (getSelectMode() != SelectMode.MULTI) {
      throw new RuntimeException("Invert selection is not supported for select mode: " + getSelectMode());
    }

    Set<T_ID> newSelectedIds = new HashSet<T_ID>(getAllIds());
    newSelectedIds.removeAll(selection.getIds());

    return setSelection(newSelectedIds);
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    ensureSelectionState();
    return selection;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean setSelection(Selection<T_ITEM> selection) {
    Selection<T_ITEM> oldSelection = this.selection;
    Selection<T_ITEM> newSelection = selection;

    // check for noop:
    if (oldSelection.getSize() == 0 &&
        newSelection.getSize() == 0) {
      return true;
    }

    try {
      fireVetoableChange(PROP_SELECTION, oldSelection, newSelection);
      this.selection = (IdQuerySelectionBase<T_ITEM, T_ID>) newSelection;
      firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
      return true;
    } catch (PropertyVetoException e) {
      LOG.debug("Selection change rejected because of a property change veto.", e);
      return false;
    }
  }

  @Override
  public Selection<T_ITEM> getAllItemsSelection() {
    return new IdQuerySelectionBase<T_ITEM, T_ID>(idQueryService, new HashSet<T_ID>(getAllIds()));
  }

  private boolean setSelection(Set<T_ID> selectedIds) {
    return setSelection(selectedIds.isEmpty()
        ? emptySelection
        : new IdQuerySelectionBase<T_ITEM, T_ID>(idQueryService, selectedIds));
  }

  private Set<T_ID> getModifiableIdSet() {
    return new HashSet<T_ID>(selection.getIds());
  }

}
