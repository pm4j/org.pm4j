package org.pm4j.common.selection;

import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.util.collection.IterableUtil;

/**
 * A {@link SelectionHandler} implementation that handles the selected items using {@link Set}.
 * May be used for in-memory collections.
 * <p>
 * It can even be used efficiently for 'unlimited' collections if {@link #selectAll(boolean)}
 * gets never called with the parameter value <code>true</code>.
 *
 * @param <T_ITEM> the type of handled items.
 *
 * @author olaf boede
 */
public class SelectionHandlerWithItemSet<T_ITEM> extends SelectionHandlerBase<T_ITEM>{

  private static final Log LOG = LogFactory.getLog(SelectionHandlerWithItemSet.class);

  @SuppressWarnings("unchecked")
  private static final ItemSetSelection<?> EMPTY_SELECTION = new ItemSetSelection<Object>(Collections.EMPTY_SET);

  /** The collection to handle selected items for. */
  // XXX oboede: should be changed to a real collection interface.
  private Iterable<T_ITEM> collection;

  /** The set of currently selected items. */
  @SuppressWarnings("unchecked")
  private ItemSetSelection<T_ITEM> selection = new ItemSetSelection<T_ITEM>(Collections.EMPTY_SET);

  /**
   * @param collection the collection to handle selected items for.
   */
  public SelectionHandlerWithItemSet(Iterable<T_ITEM> collection) {
    assert collection != null;

    this.collection = collection;
  }

  @Override
  public boolean select(boolean select, T_ITEM item) {
    assert item != null;
    Set<T_ITEM> set = getModifyableItemSet();

    if (select) {
      beforeAddSingleItemSelection(set);
      set.add(item);
    }
    else {
      set.remove(item);
    }
    return setSelection(set);
  }

  @Override
  public boolean select(boolean select, Iterable<T_ITEM> items) {
    assert items != null;
    Set<T_ITEM> set = getModifyableItemSet();

    if (getSelectMode() == SelectMode.SINGLE && select) {
      set.clear();
    }

    for (T_ITEM i : items) {
      if (select) {
        set.add(i);
      } else {
        set.remove(i);
      }
    }

    checkMultiSelectResult(set);
    return setSelection(set);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean selectAll(boolean select) {
    if (select) {
      if (getSelectMode() != SelectMode.MULTI) {
        throw new RuntimeException("Select all is not supported for select mode: " + getSelectMode());
      }

      return select(select, IterableUtil.shallowCopy(collection));
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

    Set<T_ITEM> newSelectedItems = new HashSet<T_ITEM>(IterableUtil.asCollection(collection));
    newSelectedItems.removeAll(selection.selectedItems);

    return setSelection(newSelectedItems);
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    ensureSelectionState();
    return selection;
  }

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
      this.selection = (ItemSetSelection<T_ITEM>)newSelection;
      firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
      return true;
    } catch (PropertyVetoException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Selection change rejected because of a property change veto. " + e.getMessage());
      }
      return false;
    }
  }

  @Override
  public Selection<T_ITEM> getAllItemsSelection() {
    return new ItemSetSelection<T_ITEM>(new HashSet<T_ITEM>(IterableUtil.asCollection(collection)));
  }


  @SuppressWarnings("unchecked")
  private boolean setSelection(Set<T_ITEM> selectedItems) {
    return setSelection(selectedItems.isEmpty()
                    ? selection = (ItemSetSelection<T_ITEM>) EMPTY_SELECTION
                    : new ItemSetSelection<T_ITEM>(selectedItems));
  }

  private Set<T_ITEM> getModifyableItemSet() {
    return new HashSet<T_ITEM>(selection.selectedItems);
  }

}
