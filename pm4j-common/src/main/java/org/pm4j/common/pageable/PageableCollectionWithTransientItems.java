package org.pm4j.common.pageable;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.EmptySelection;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerBase;
import org.pm4j.common.selection.SelectionHandlerWithItemSet;

/**
 * A pageable collection that combines items provided by a backing base
 * {@link PageableCollection2} with a list of transient items.
 *
 * @param <T_ITEM>
 *          the type of handled items.
 *
 * @author olaf boede
 */
public class PageableCollectionWithTransientItems<T_ITEM> implements PageableCollection2<T_ITEM> {
  private static final Log                   LOG              = LogFactory.getLog(SelectionHandlerWithItemSet.class);

  private final PageableCollection2<T_ITEM>  baseCollection;
  private List<T_ITEM>                       transientItems   = new ArrayList<T_ITEM>();
  private SelectionHandlerWithTransientItems selectionHandler = new SelectionHandlerWithTransientItems();

  /**
   * @param baseCollection
   */
  public PageableCollectionWithTransientItems(PageableCollection2<T_ITEM> baseCollection) {
    assert baseCollection != null;
    this.baseCollection = baseCollection;
  }

  /**
   * Adds a transient item to handle.
   *
   * @param item
   *          the new item.
   */
  public void addTransientItem(T_ITEM item) {
    transientItems.add(item);
  }

  /**
   * Removes a transient item.
   *
   * @param item
   *          the transient item to delete.
   */
  public void removeTransientItem(T_ITEM item) {
    transientItems.remove(item);
  }

  /**
   * Provides the set of all transient items.
   *
   * @return the transient item set.
   */
  public List<T_ITEM> getTransientItems() {
    return transientItems;
  }

  /**
   * Clears all transient items.
   */
  public void clearTransientItems() {
    transientItems.clear();
  }

  /**
   * Provides the subset of all non-transient selected items.
   *
   * @return the selection.
   */
  public Selection<T_ITEM> getPersistentItemSelection() {
    return baseCollection.getSelectionHandler().getSelection();
  }

  @Override
  public QueryParams getQueryParams() {
    return baseCollection.getQueryParams();
  }

  @Override
  public QueryOptions getQueryOptions() {
    return baseCollection.getQueryOptions();
  }

  @Override
  public List<T_ITEM> getItemsOnPage() {
    if (transientItems.isEmpty()) {
      return baseCollection.getItemsOnPage();
    } else {
      List<T_ITEM> list = new ArrayList<T_ITEM>(baseCollection.getItemsOnPage());
      list.addAll(transientItems);
      return list;
    }
  }

  @Override
  public int getPageSize() {
    return baseCollection.getPageSize();
  }

  @Override
  public void setPageSize(int newSize) {
    baseCollection.setPageSize(newSize);
  }

  @Override
  public int getCurrentPageIdx() {
    return baseCollection.getCurrentPageIdx();
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    baseCollection.setCurrentPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return baseCollection.getNumOfItems() + transientItems.size();
  }

  @Override
  public long getUnfilteredItemCount() {
    return baseCollection.getUnfilteredItemCount() + transientItems.size();
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new IteratorWithAdditionalItems<T_ITEM>(baseCollection.iterator(), transientItems.iterator());
  }

  @Override
  public SelectionHandler<T_ITEM> getSelectionHandler() {
    return selectionHandler;
  }

  @Override
  public void clearCaches() {
    baseCollection.clearCaches();
  }

  public class SelectionHandlerWithTransientItems extends SelectionHandlerBase<T_ITEM> {

    private SelectionWithTransientItems<T_ITEM> emptySelection = new SelectionWithTransientItems<T_ITEM>(
                                                                   EmptySelection.<T_ITEM> getEmptySelection());

    /** The set of currently selected items. */
    private SelectionWithTransientItems<T_ITEM> selection      = emptySelection;

    @Override
    public void setSelectMode(SelectMode selectMode) {
      super.setSelectMode(selectMode);
      baseCollection.getSelectionHandler().setSelectMode(selectMode);
    }

    @Override
    public boolean select(boolean select, T_ITEM item) {
      if (transientItems.contains(item)) {
        SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(
            selection.baseSelection);
        newSelection.selectedTransientItems = new ArrayList<T_ITEM>(selection.selectedTransientItems);
        if (select) {
          newSelection.selectedTransientItems.add(item);
        } else {
          newSelection.selectedTransientItems.remove(item);
        }
        return setSelection(newSelection);
      } else {
        if (baseCollection.getSelectionHandler().select(select, item)) {
          SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(baseCollection
              .getSelectionHandler().getSelection());
          newSelection.selectedTransientItems = selection.selectedTransientItems;
          return setSelection(newSelection);
        } else {
          return false;
        }
      }
    }

    @Override
    public boolean select(boolean select, Iterable<T_ITEM> items) {
      List<T_ITEM> newTransientItems = new ArrayList<T_ITEM>();
      List<T_ITEM> newBaseItems = new ArrayList<T_ITEM>();

      for (T_ITEM i : items) {
        if (transientItems.contains(i)) {
          newTransientItems.add(i);
        } else {
          newBaseItems.add(i);
        }
      }

      if (!newBaseItems.isEmpty()) {
        if (!baseCollection.getSelectionHandler().select(select, newBaseItems)) {
          return false;
        }
      }

      SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(baseCollection
          .getSelectionHandler().getSelection());
      newSelection.selectedTransientItems = new ArrayList<T_ITEM>(selection.selectedTransientItems);

      for (T_ITEM i : newTransientItems) {
        if (select) {
          newSelection.selectedTransientItems.add(i);
        } else {
          newSelection.selectedTransientItems.remove(i);
        }
      }

      return setSelection(newSelection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean selectAll(boolean select) {
      if (!baseCollection.getSelectionHandler().selectAll(select)) {
        return false;
      }
      SelectionWithTransientItems<T_ITEM> newSelection = new SelectionWithTransientItems<T_ITEM>(baseCollection
          .getSelectionHandler().getSelection());
      newSelection.selectedTransientItems = select ? Collections.unmodifiableList(transientItems)
          : Collections.EMPTY_LIST;

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

      SelectionWithTransientItems<T_ITEM> newSel = new SelectionWithTransientItems<T_ITEM>(baseCollection.getSelectionHandler().getSelection());
      newSel.selectedTransientItems = new ArrayList<T_ITEM>(transientItems);
      newSel.selectedTransientItems.removeAll(selection.selectedTransientItems);

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
        this.selection = (SelectionWithTransientItems<T_ITEM>) newSelection;
        firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
        return true;
      } catch (PropertyVetoException e) {
        LOG.debug("Selection change rejected because of a property change veto.", e);
        return false;
      }
    }

  }

  static class SelectionWithTransientItems<T_ITEM> implements Selection<T_ITEM> {
    private static final long       serialVersionUID = 1L;
    private final Selection<T_ITEM> baseSelection;
    @SuppressWarnings("unchecked")
    private List<T_ITEM>            selectedTransientItems = Collections.EMPTY_LIST;

    public SelectionWithTransientItems(Selection<T_ITEM> baseSelection) {
      assert baseSelection != null;
      this.baseSelection = baseSelection;
    }

    @Override
    public long getSize() {
      return baseSelection.getSize() + selectedTransientItems.size();
    }

    @Override
    public boolean contains(T_ITEM item) {
      return baseSelection.contains(item) || selectedTransientItems.contains(item);
    }

    @Override
    public Iterator<T_ITEM> iterator() {
      return new IteratorWithAdditionalItems<T_ITEM>(baseSelection.iterator(), selectedTransientItems.iterator());
    }

    /** Block size has no effect on this iterator implementation. */
    @Override
    public void setIteratorBlockSizeHint(int readBlockSize) {
    }

    @Override
    public <T_BEAN> Selection<T_BEAN> getBeanSelection() {
      if (!selectedTransientItems.isEmpty()) {
        throw new NotImplementedException("Unable to provide a bean selection as long as there are transient items.");
      }
      return baseSelection.getBeanSelection();
    }

  }

  static class IteratorWithAdditionalItems<T_ITEM> implements Iterator<T_ITEM> {

    private final Iterator<T_ITEM> baseIterator;
    private final Iterator<T_ITEM> secondIterator;

    public IteratorWithAdditionalItems(Iterator<T_ITEM> baseIterator, Iterator<T_ITEM> secondIterator) {
      assert baseIterator != null;
      assert secondIterator != null;

      this.baseIterator = baseIterator;
      this.secondIterator = secondIterator;
    }

    @Override
    public boolean hasNext() {
      return baseIterator.hasNext() || secondIterator.hasNext();
    }

    @Override
    public T_ITEM next() {
      return baseIterator.hasNext() ? baseIterator.next() : secondIterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }
}
