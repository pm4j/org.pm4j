package org.pm4j.common.selection;

import static org.apache.commons.lang.Validate.notNull;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.pm4j.common.util.collection.IterableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author alech
 */
public abstract class ItemSetSelectionHandler<T_ITEM> extends SelectionHandlerBase<T_ITEM>{

  private static final Logger LOG = LoggerFactory.getLogger(ItemSetSelectionHandler.class);

  @SuppressWarnings("unchecked")
  private static final ItemSetSelection<?> EMPTY_SELECTION = new ItemSetSelection<Object>(Collections.EMPTY_SET);

  /** The set of currently selected items. */
  @SuppressWarnings("unchecked")
  private ItemSetSelection<T_ITEM> selection = new ItemSetSelection<T_ITEM>(Collections.EMPTY_SET);

  @Override
  public boolean select(boolean select, T_ITEM item) {
    notNull(item, "item cannot be null");
    Set<T_ITEM> newSelection = new LinkedHashSet<T_ITEM>((int) selection.getSize() + 1);
    
    //rebuild selection maintaining original order
    
    if(select) { //select
      if (getSelectMode().equals(SelectMode.SINGLE)) {
        newSelection.add(item);
      } else if (getSelectMode().equals(SelectMode.MULTI)) {
        for (T_ITEM allCollectionItem : getAllCollection()) {
          if (selection.contains(allCollectionItem) || allCollectionItem.equals(item)) {
            //item was previously selected or is intended to -> pass it to the new selection set
            newSelection.add(allCollectionItem);
          }
          if (newSelection.size() ==  selection.getSize() + 1) {
            break; // got them all!
          }
        }
      } else {
        throw new RuntimeException("Selection for select mode '" + getSelectMode() + "' is not supported.");
      }
    }  else {  //unselect
      newSelection.addAll(IterableUtil.asCollection(selection));
      newSelection.remove(item);
    }
    
    return setSelection(newSelection);
  }

  @Override
  public boolean select(boolean select, Iterable<T_ITEM> items) {
    notNull(items, "items cannot be null");
    Set<T_ITEM> newSelection = new LinkedHashSet<T_ITEM>();
    Set<T_ITEM> itemsToProcess = new LinkedHashSet<T_ITEM>(IterableUtil.asCollection(items));

    // rebuild selection maintaining original order
    if (select) { // select
      for (T_ITEM allCollectionItem : getAllCollection()) {
        if (selection.contains(allCollectionItem) || itemsToProcess.contains(allCollectionItem)) {
          newSelection.add(allCollectionItem);
        }
        if (newSelection.size() == selection.getSize() + itemsToProcess.size()) {
          break; // got them all!
        }
      }
    } else { // unselect
      newSelection.addAll(IterableUtil.asCollection(selection));
      newSelection.removeAll(itemsToProcess);
    }

    checkMultiSelectResult(newSelection);
    return setSelection(newSelection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean selectAll(boolean select) {
    if (select) {
      if (getSelectMode() != SelectMode.MULTI) {
        throw new RuntimeException("Select all is not supported for select mode: " + getSelectMode());
      }

      return setSelection(new LinkedHashSet<T_ITEM>(getAllCollection()));
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

    Set<T_ITEM> newSelectedItems = new LinkedHashSet<T_ITEM>(getAllCollection());
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
    return new ItemSetSelection<T_ITEM>(new LinkedHashSet<T_ITEM>(getAllCollection()));
  }


  @SuppressWarnings("unchecked")
  private boolean setSelection(Set<T_ITEM> selectedItems) {
    return setSelection(selectedItems.isEmpty()
                    ? (ItemSetSelection<T_ITEM>) EMPTY_SELECTION
                    : new ItemSetSelection<T_ITEM>(selectedItems));
  }
  
  /**
   * 
   * @return a collection containing all available items possible to select
   */
  protected abstract Collection<T_ITEM> getAllCollection();

}
