package org.pm4j.common.selection;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.pageable.PageableCollection2;

public class SelectionHandlerWithAdditionalItems<T_ITEM> extends SelectionHandlerBase<T_ITEM> {
  private static final Log                   LOG              = LogFactory.getLog(SelectionHandlerWithAdditionalItems.class);

  private SelectionWithAdditionalItems<T_ITEM> emptySelection = new SelectionWithAdditionalItems<T_ITEM>(
                                                                 EmptySelection.<T_ITEM> getEmptySelection(), null);

  private final PageableCollection2<T_ITEM>  baseCollection;
  private final List<T_ITEM>                 additionalItems;

  /** The set of currently selected items. */
  private SelectionWithAdditionalItems<T_ITEM> selection      = emptySelection;

  public SelectionHandlerWithAdditionalItems(PageableCollection2<T_ITEM>  baseCollection, List<T_ITEM> transientItems) {
    assert baseCollection != null;
    assert transientItems != null;

    this.baseCollection = baseCollection;
    this.additionalItems = transientItems;
  }

  @Override
  public void setSelectMode(SelectMode selectMode) {
    super.setSelectMode(selectMode);
    baseCollection.getSelectionHandler().setSelectMode(selectMode);
  }

  // TODO olaf: will be simplified and reduced to a single selection change event when the 'addition item'
  // case gets handled by the query modification/selection handler too.
  @Override
  public boolean select(boolean select, T_ITEM item) {
    if (additionalItems.contains(item)) {
      Collection<T_ITEM> transientItemSelection = new ArrayList<T_ITEM>(selection.getAdditionalSelectedItems());
      Selection<T_ITEM> baseSelection = selection.getBaseSelection();
      if (select) {
        if (getSelectMode() == SelectMode.SINGLE) {
          // the selection switches to the transient item. All other selections need to be removed.
          if (!baseCollection.getSelectionHandler().selectAll(false)) {
            return false;
          }
          baseSelection = baseCollection.getSelectionHandler().getSelection();
          transientItemSelection.clear();
        }
        transientItemSelection.add(item);
      } else {
        transientItemSelection.remove(item);
      }

     SelectionWithAdditionalItems<T_ITEM> newSelection = new SelectionWithAdditionalItems<T_ITEM>(
            baseSelection,
            transientItemSelection);
      return setSelection(newSelection);
    } else {
      if (!baseCollection.getSelectionHandler().select(select, item)) {
        return false;
      }

      SelectionWithAdditionalItems<T_ITEM> newSelection = new SelectionWithAdditionalItems<T_ITEM>(
          baseCollection.getSelectionHandler().getSelection(),
          selection.getAdditionalSelectedItems());
      return setSelection(newSelection);
    }
  }

  @Override
  public boolean select(boolean select, Iterable<T_ITEM> items) {
    List<T_ITEM> newTransientItems = new ArrayList<T_ITEM>();
    List<T_ITEM> newBaseItems = new ArrayList<T_ITEM>();

    for (T_ITEM i : items) {
      if (additionalItems.contains(i)) {
        newTransientItems.add(i);
      } else {
        newBaseItems.add(i);
      }
    }

    // try to change the base collection selection.
    // if that fails, we simply can skip this operation without side effects.
    if (!newBaseItems.isEmpty()) {
      if (!baseCollection.getSelectionHandler().select(select, newBaseItems)) {
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
        baseCollection.getSelectionHandler().getSelection(),
        selectedTransientItems);
    return setSelection(newSelection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean selectAll(boolean select) {
    if (!baseCollection.getSelectionHandler().selectAll(select)) {
      return false;
    }
    SelectionWithAdditionalItems<T_ITEM> newSelection = new SelectionWithAdditionalItems<T_ITEM>(
        baseCollection.getSelectionHandler().getSelection(),
        select ? additionalItems : Collections.EMPTY_LIST);

    return setSelection(newSelection);
  }

  @Override
  public boolean invertSelection() {
    if (getSelectMode() != SelectMode.MULTI) {
      throw new RuntimeException("Invert selection is not supported for select mode: " + getSelectMode());
    }

    if (!baseCollection.getSelectionHandler().invertSelection()) {
      return false;
    }

    Collection<T_ITEM> transientSelectedItems = new HashSet<T_ITEM>(additionalItems);
    transientSelectedItems.removeAll(selection.getAdditionalSelectedItems());
    SelectionWithAdditionalItems<T_ITEM> newSel = new SelectionWithAdditionalItems<T_ITEM>(
          baseCollection.getSelectionHandler().getSelection(),
          transientSelectedItems);

    if (!setSelection(newSel)) {
      baseCollection.getSelectionHandler().invertSelection();
      return false;
    } else {
      return true;
    }
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    return selection;
  }

  @Override
  public boolean setSelection(Selection<T_ITEM> selection) {
    Selection<T_ITEM> oldSelection = this.selection;
    Selection<T_ITEM> newSelection = selection;

    try {
      fireVetoableChange(PROP_SELECTION, oldSelection, newSelection);
      this.selection = (SelectionWithAdditionalItems<T_ITEM>) newSelection;
      firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
      return true;
    } catch (PropertyVetoException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Selection change rejected because of a property change veto. " + e.getMessage());
      }
      return false;
    }
  }

}