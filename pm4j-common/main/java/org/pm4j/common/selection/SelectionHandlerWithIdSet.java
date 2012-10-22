package org.pm4j.common.selection;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class SelectionHandlerWithIdSet<T_ITEM, T_ID> extends SelectionHandlerBase<T_ITEM> {

  private static final Log LOG = LogFactory.getLog(SelectionHandlerWithIdSet.class);

  /** Used to provide empty selections. */
  private final ItemIdBasedSelection<T_ITEM, T_ID> emptySelection;

  /** The converter used to get items for the internally handled id's. */
  private final ItemIdConverter<T_ITEM, T_ID> itemIdConverter;

  /** The current selection. */
  private ItemIdBasedSelection<T_ITEM, T_ID> selection;

  @SuppressWarnings("unchecked")
  public SelectionHandlerWithIdSet(ItemIdConverter<T_ITEM, T_ID> itemIdConverter) {
    assert itemIdConverter != null;

    this.itemIdConverter = itemIdConverter;
    this.emptySelection = new ItemIdBasedSelection<T_ITEM, T_ID>(itemIdConverter, Collections.EMPTY_SET);
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
    T_ID id = itemIdConverter.getIdForItem(item);
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
        ids.add(itemIdConverter.getIdForItem(i));
      }
      else {
        ids.remove(itemIdConverter.getIdForItem(i));
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
  public Selection<T_ITEM> getSelection() {
    return selection;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean setSelection(Selection<T_ITEM> selection) {
    Selection<T_ITEM> oldSelection = this.selection;
    Selection<T_ITEM> newSelection = selection;

    try {
      fireVetoableChange(PROP_SELECTION, oldSelection, newSelection);
      this.selection = (ItemIdBasedSelection<T_ITEM, T_ID>) newSelection;
      firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
      return true;
    } catch (PropertyVetoException e) {
      LOG.debug("Selection change rejected because of a property change veto.", e);
      return false;
    }
  }

  private boolean setSelection(Set<T_ID> selectedIds) {
    return setSelection(selectedIds.isEmpty()
        ? emptySelection
        : new ItemIdBasedSelection<T_ITEM, T_ID>(itemIdConverter, selectedIds));
  }

  private Set<T_ID> getModifiableIdSet() {
    return new HashSet<T_ID>(selection.ids);
  }

}
