package org.pm4j.common.pageable.querybased.idquery;

import static org.apache.commons.lang.Validate.notNull;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link SelectionHandler} for {@link IdQueryCollectionImpl}s.
 *
 * @param <T_ITEM> type of items to handle.
 * @param <T_ID> type of item id's.
 *
 * @author Olaf Boede
 * @author alech
 */
public abstract class IdQuerySelectionHandler<T_ITEM, T_ID> extends SelectionHandlerBase<T_ITEM> {

  private static final Logger LOG = LoggerFactory.getLogger(IdQuerySelectionHandler.class);

  /** Used to provide empty selections. */
  private final IdQuerySelection<T_ITEM, T_ID> emptySelection;

  /** The converter used to get items for the internally handled id's. */
  private final IdQueryService<T_ITEM, T_ID> idQueryService;

  /** The current selection. */
  private IdQuerySelection<T_ITEM, T_ID> selection;

  @SuppressWarnings("unchecked")
  public IdQuerySelectionHandler(IdQueryService<T_ITEM, T_ID> idQueryService) {
    assert idQueryService != null;

    this.idQueryService = idQueryService;
    this.emptySelection = new IdQuerySelection<T_ITEM, T_ID>(idQueryService, Collections.EMPTY_SET);
    this.selection = emptySelection;
  }

  /**
   * @return a collection containing all available item ids possible to select
   */
  protected abstract Collection<T_ID> getAllIds();

   @Override
  public boolean select(boolean select, T_ITEM item) {
    notNull(item, "item cannot be null");
    Set<T_ID> newSelection = new LinkedHashSet<T_ID>((int) selection.getSize() + 1);
    T_ID itemId = idQueryService.getIdForItem(item);

    //rebuild selection maintaining original order

    if (select) { //select
      if (getSelectMode().equals(SelectMode.SINGLE)) {
        newSelection.add(itemId);
      } else if (getSelectMode().equals(SelectMode.MULTI)) {
        for (T_ID allCollectionItemId : getAllIds()) {
          if (selection.getIds().contains(allCollectionItemId) || allCollectionItemId.equals(itemId)) {
            //item was previously selected or is intended to -> pass it to the new selection set
            newSelection.add(allCollectionItemId);
          }
          if (newSelection.size() ==  selection.getSize() + 1) {
            break; // got them all!
          }
        }
      } else {
        throw new RuntimeException("Selection for select mode '" + getSelectMode() + "' is not supported.");
      }
    } else { //unselect
      newSelection.addAll(selection.getIds());
      newSelection.remove(itemId);
    }

    return setSelection(newSelection);
  }

  @Override
  public boolean select(boolean select, Iterable<T_ITEM> items) {
    Set<T_ID> newSelection = new LinkedHashSet<T_ID>();

    //prepare ids
    Set<T_ID> itemIds = new LinkedHashSet<T_ID>();
    for(T_ITEM item : items) {
      itemIds.add(idQueryService.getIdForItem(item));
    }

    //rebuild selection maintaining original order
    if (select) { //select
      for (T_ID allCollectionItemId : getAllIds()) {
        if (selection.getIds().contains(allCollectionItemId) || itemIds.contains(allCollectionItemId)) {
          newSelection.add(allCollectionItemId);
        }
        if (newSelection.size() == selection.getSize() + itemIds.size()) {
          break; // got them all!
        }
      }
    } else { //unselect
      newSelection.addAll(selection.getIds());
      newSelection.removeAll(itemIds);
    }

    checkMultiSelectResult(newSelection);
    return setSelection(newSelection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean selectAll(boolean select) {
    if (select) {
      if (getSelectMode() != SelectMode.MULTI) {
        throw new RuntimeException("Select all for current select mode is not supported: " + getSelectMode());
      }

      return setSelection(new LinkedHashSet<T_ID>(getAllIds()));
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

    Set<T_ID> newSelectedIds = new LinkedHashSet<T_ID>(getAllIds());
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
      this.selection = (IdQuerySelection<T_ITEM, T_ID>) newSelection;
      firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
      return true;
    } catch (PropertyVetoException e) {
      LOG.debug("Selection change rejected because of a property change veto.", e);
      return false;
    }
  }

  @Override
  public Selection<T_ITEM> getAllItemsSelection() {
    return new IdQuerySelection<T_ITEM, T_ID>(idQueryService, getAllIds());
  }

  private boolean setSelection(Set<T_ID> selectedIds) {
    return setSelection(selectedIds.isEmpty()
        ? emptySelection
        : new IdQuerySelection<T_ITEM, T_ID>(idQueryService, selectedIds));
  }

}
