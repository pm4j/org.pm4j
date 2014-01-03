package org.pm4j.common.selection;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.pageable.PageableCollection;

/**
 * Handles in front of another {@link SelectionHandler} selected transient items.
 * <p>
 * It obtains the set of transient items from the modifications of the related
 * {@link PageableCollection}.
 *
 * @author olaf boede
 *
 * @param <T_ITEM> type of items to select.
 */
public class SelectionHandlerWithAdditionalItems<T_ITEM> extends SelectionHandlerBase<T_ITEM> {
  private static final Log                     LOG            = LogFactory.getLog(SelectionHandlerWithAdditionalItems.class);

  private SelectionWithAdditionalItems<T_ITEM> emptySelection = new SelectionWithAdditionalItems<T_ITEM>(
                                                                 EmptySelection.<T_ITEM> getEmptySelection(), null);

  private final PageableCollection<T_ITEM>    baseCollection;
  private final SelectionHandler<T_ITEM>       baseSelectionHandler;

  /** The set of currently selected items. */
  private SelectionWithAdditionalItems<T_ITEM> selection      = emptySelection;

  /**
   * @param baseCollection
   *          the backing collection to handle selections for.
   * @param transientItems
   *          a reference to the set of additional transient items to consider.
   */
  public SelectionHandlerWithAdditionalItems(PageableCollection<T_ITEM> baseCollection, SelectionHandler<T_ITEM> baseSelectionHandler) {
    assert baseCollection != null;
    assert baseSelectionHandler != null;
    assert !baseSelectionHandler.isFirePropertyEvents() : "Event handling of the nested selection handler should be disabled.";

    this.baseCollection = baseCollection;
    this.baseSelectionHandler = baseSelectionHandler;
  }

  @Override
  public void setSelectMode(SelectMode selectMode) {
    super.setSelectMode(selectMode);
    baseSelectionHandler.setSelectMode(selectMode);
  }

  @Override
  public boolean select(boolean select, T_ITEM item) {
    assert ! baseSelectionHandler.isFirePropertyEvents();

    Selection<T_ITEM> oldBaseSelection = selection.getBaseSelection();
    boolean success = false;

    try {
      if (getAdditionalItems().contains(item)) {
        Collection<T_ITEM> transientItemSelection = new ArrayList<T_ITEM>(selection.getAdditionalSelectedItems());
        Selection<T_ITEM> baseSelection = oldBaseSelection;
        if (select) {
          if (getSelectMode() == SelectMode.SINGLE) {
            // the selection switches to the transient item. All other selections need to be removed.
            if (! baseSelectionHandler.selectAll(false)) {
              return false;
            }
            baseSelection = baseSelectionHandler.getSelection();
            transientItemSelection.clear();
          }
          transientItemSelection.add(item);
        } else {
          transientItemSelection.remove(item);
        }

        SelectionWithAdditionalItems<T_ITEM> newSelection = new SelectionWithAdditionalItems<T_ITEM>(
              baseSelection,
              transientItemSelection);
        success = setSelection(newSelection);
      } else {
        if (! baseSelectionHandler.select(select, item)) {
          return false;
        }

        SelectionWithAdditionalItems<T_ITEM> newSelection = new SelectionWithAdditionalItems<T_ITEM>(
            baseSelectionHandler.getSelection(),
            getSelectMode() == SelectMode.SINGLE
                ? null
                : selection.getAdditionalSelectedItems());
        success = setSelection(newSelection);
      }
    } finally {
      if (!success) {
        baseSelectionHandler.setSelection(oldBaseSelection);
      }
    }

    return success;
  }

  @Override
  public boolean select(boolean select, Iterable<T_ITEM> items) {
    assert ! baseSelectionHandler.isFirePropertyEvents();

    Selection<T_ITEM> oldBaseSelection = selection.getBaseSelection();
    boolean success = false;

    try {
      List<T_ITEM> newTransientItems = new ArrayList<T_ITEM>();
      List<T_ITEM> newBaseItems = new ArrayList<T_ITEM>();

      for (T_ITEM i : items) {
        if (getAdditionalItems().contains(i)) {
          newTransientItems.add(i);
        } else {
          newBaseItems.add(i);
        }
      }

      // try to change the base collection selection.
      // if that fails, we simply can skip this operation without side effects.
      if (!newBaseItems.isEmpty()) {
        if (! baseSelectionHandler.select(select, newBaseItems)) {
          return false;
        }
      }

      Collection<T_ITEM> selectedTransientItems = new HashSet<T_ITEM>(selection.getAdditionalSelectedItems());
      for (T_ITEM i : newTransientItems) {
        if (select) {
          selectedTransientItems.add(i);
        } else {
          selectedTransientItems.remove(i);
        }
      }
      SelectionWithAdditionalItems<T_ITEM> newSelection = new SelectionWithAdditionalItems<T_ITEM>(
          baseSelectionHandler.getSelection(),
          selectedTransientItems);

      success = setSelection(newSelection);
    } finally {
      if (!success) {
        baseSelectionHandler.setSelection(oldBaseSelection);
      }
    }

    return success;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean selectAll(boolean select) {
    assert ! baseSelectionHandler.isFirePropertyEvents();

    Selection<T_ITEM> oldBaseSelection = selection.getBaseSelection();
    boolean success = false;

    try {
      if (baseSelectionHandler.selectAll(select)) {
        SelectionWithAdditionalItems<T_ITEM> newSelection = new SelectionWithAdditionalItems<T_ITEM>(
            baseSelectionHandler.getSelection(),
            select
                ? getAdditionalItems()
                : Collections.EMPTY_LIST);

        success = setSelection(newSelection);
      }
    } finally {
      if (!success) {
        baseSelectionHandler.setSelection(oldBaseSelection);
      }
    }

    return success;
  }

  @Override
  public boolean invertSelection() {
    assert ! baseSelectionHandler.isFirePropertyEvents();

    if (getSelectMode() != SelectMode.MULTI) {
      throw new RuntimeException("Invert selection is not supported for select mode: " + getSelectMode());
    }

    Selection<T_ITEM> oldBaseSelection = selection.getBaseSelection();
    boolean success = false;

    try {
      if (baseSelectionHandler.invertSelection()) {
        Collection<T_ITEM> transientSelectedItems = new HashSet<T_ITEM>(getAdditionalItems());
        transientSelectedItems.removeAll(selection.getAdditionalSelectedItems());
        SelectionWithAdditionalItems<T_ITEM> newSel = new SelectionWithAdditionalItems<T_ITEM>(
              baseSelectionHandler.getSelection(),
              transientSelectedItems);

        success = setSelection(newSel);
      }
    } finally {
      if (!success) {
        baseSelectionHandler.setSelection(oldBaseSelection);
      }
    }

    return success;
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    ensureSelectionState();
    return selection;
  }

  @Override
  public boolean setSelection(Selection<T_ITEM> selection) {
    assert selection instanceof SelectionWithAdditionalItems;
    assert ! baseSelectionHandler.isFirePropertyEvents();

    Selection<T_ITEM> oldSelection = this.selection;
    SelectionWithAdditionalItems<T_ITEM> newSelection = (SelectionWithAdditionalItems<T_ITEM>) selection;

    // check for noop:
    if (oldSelection.isEmpty() && newSelection.isEmpty()) {
    	return true;
    }

    try {
      fireVetoableChange(PROP_SELECTION, oldSelection, newSelection);
      this.baseSelectionHandler.setSelection(newSelection.getBaseSelection());
      this.selection = newSelection;
      firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
      return true;
    } catch (PropertyVetoException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Selection change rejected because of a property change veto. " + e.getMessage());
      }
      return false;
    }
  }

  protected List<T_ITEM> getAdditionalItems() {
    return baseCollection.getModifications().getAddedItems();
  }

}