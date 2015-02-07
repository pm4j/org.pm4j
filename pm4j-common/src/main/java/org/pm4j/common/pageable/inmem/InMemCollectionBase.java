package org.pm4j.common.pageable.inmem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.modifications.ModificationsImpl;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionBase;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.query.inmem.InMemQueryEvaluatorSet;
import org.pm4j.common.selection.ItemSetSelection;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.selection.ItemSetSelectionHandler;
import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.common.util.collection.ListUtil;

/**
 * Implements a {@link PageableCollection} based on an {@link List} of items to
 * handle.
 * 
 * @author olaf boede
 * 
 * @param <T_ITEM>
 *          The type of items handled by this set.
 */
public abstract class InMemCollectionBase<T_ITEM>
  extends PageableCollectionBase<T_ITEM>
  implements InMemCollection<T_ITEM> {

  /** The collection type specific selection handler. */
  private SelectionHandler<T_ITEM> selectionHandler = new ItemSetSelectionHandler<T_ITEM>(this);
  /** In-memory specific item set modification handler. */
  private ModificationHandler<T_ITEM> modificationHandler = new InMemModificationHandler();
  /** Interpreter for filtering query expressions. */
  private InMemQueryEvaluator<T_ITEM>    inMemQueryEvaluator = new InMemQueryEvaluator<T_ITEM>(InMemQueryEvaluatorSet.INSTANCE);

  /** The cache strategy used for backing collection. */
  private CacheStrategy cacheStrategy = CacheStrategyNoCache.INSTANCE;
  /** The cache strategy specific context used to hold the cached value. */
  private Object cacheCtxt;
  /** The current set of filtered and sorted items. */
  private List<T_ITEM> filteredAndSortedObjects;
  /** The currently active sort order comparator. */
  private Comparator<T_ITEM> sortOrderComparator;

  /** A listener gets called if a query property gets changed that affects the effective filter result. */
  private PropertyChangeListener changeFilterListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      filteredAndSortedObjects = null;
    }
  };
  /** A listener gets called if a query property gets changed that affects the sort order of the items to show. */
  private PropertyChangeListener changeSortOrderListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      sortOrderComparator = null;
      filteredAndSortedObjects = null;
    }
  };

  /**
   * @param queryOptions
   *          The set of query options offered that's usually offered to the user.<br>
   *          May be <code>null</code> if there are no predefined query options.
   */
  public InMemCollectionBase(QueryOptions queryOptions) {
    super(queryOptions);
    // getQueryParams() is used because the super ctor may have created it on the fly (in case of a null parameter)
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, changeFilterListener);
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, changeSortOrderListener);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final Collection<T_ITEM> getBackingCollection() {
    Object o = cacheStrategy.getCachedValue(cacheCtxt);
    if (o != CacheStrategy.NO_CACHE_VALUE) {
      return (Collection<T_ITEM>) o;
    }
    else {
      Collection<T_ITEM> c = getBackingCollectionImpl();
      return cacheStrategy.setAndReturnCachedValue(cacheCtxt, c != null
                  ? c
                  : Collections.EMPTY_LIST);
    }
  }

  /**
   * Concrete classes provide here access to the handled backing collection.
   * 
   * @return the handled collection. May be <code>null</code>.
   */
  protected abstract Collection<T_ITEM> getBackingCollectionImpl();

  @Override
  public List<T_ITEM> getItemsOnPage() {
    List<T_ITEM> objects = _getObjects();
    if (objects.isEmpty()) {
      return Collections.emptyList();
    }

    int first = PageableCollectionUtil.getIdxOfFirstItemOnPageAsInt(this) - 1;
    int last = PageableCollectionUtil.getIdxOfLastItemOnPageAsInt(this);

    if (first > last) {
      return Collections.emptyList();
    }

    return objects.subList(first, last);
  }

  @Override
  public long getNumOfItems() {
    return _getObjects().size();
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return _getObjects().iterator();
  }

  @Override
  public SelectionHandler<T_ITEM> getSelectionHandler() {
    return selectionHandler;
  }

  @Override
  public ModificationHandler<T_ITEM> getModificationHandler() {
    return modificationHandler;
  }

  @Override
  public void clearCaches() {
    sortOrderComparator = null;
    filteredAndSortedObjects = null;
    cacheStrategy.clear(cacheCtxt);
  }

  /**
   * @param inMemQueryEvaluator the inMemQueryEvaluator to set
   */
  @Override
  public void setInMemQueryEvaluator(InMemQueryEvaluator<T_ITEM> inMemQueryEvaluator) {
    assert inMemQueryEvaluator != null;
    this.inMemQueryEvaluator = inMemQueryEvaluator;
  }

  /**
   * @param cacheStrategy The cache strategy used for backing collection.
   * @param cacheCtxt The cache strategy specific context used to hold the cached value.
   */
  public void setCacheStrategy(CacheStrategy cacheStrategy, Object cacheCtxt) {
    assert cacheStrategy != null;
    this.cacheStrategy = cacheStrategy;
    this.cacheCtxt = cacheCtxt;
  }

  private Comparator<T_ITEM> _getSortOrderComparator() {
    if (sortOrderComparator == null) {
      sortOrderComparator = inMemQueryEvaluator.getComparator(getQueryParams().getEffectiveSortOrder());
    }
    return sortOrderComparator;
  }

  private List<T_ITEM> _getObjects() {
    if (filteredAndSortedObjects == null) {
      Collection<T_ITEM> backingCollection = getBackingCollection();
      if (!getQueryParams().isExecQuery()) {
        filteredAndSortedObjects = Collections.emptyList();
      }
      else {
        List<T_ITEM> list = _filter(new ArrayList<T_ITEM>(backingCollection));
        Comparator<T_ITEM> comparator = _getSortOrderComparator();

        if (comparator != null) {
          Collections.sort(list, comparator);
        }

        filteredAndSortedObjects = list;
      }

      // XXX olaf: just moves to the last possible page if necessary.
      // The user may want to stay on the page with his selected item.
      // We need to define strategies for application specific definitions.
      PageableCollectionUtil.ensureCurrentPageInRange(this);
    }

    return filteredAndSortedObjects;
  }

  /** Generates a list of filtered items based on the given list. */
  private List<T_ITEM> _filter(List<T_ITEM> unfilteredList) {
    QueryExpr filterExpression = getQueryParams().getQueryExpression();
    if (filterExpression == null) {
      return unfilteredList;
    }

    List<T_ITEM> filteredList = inMemQueryEvaluator.evaluateSubSet(unfilteredList, filterExpression);
    return filteredList;
  }

  protected class InMemModificationHandler implements ModificationHandler<T_ITEM> {

    private ModificationsImpl<T_ITEM> modifications = new ModificationsImpl<T_ITEM>();

    /**
     * Adds the item as the last one.
     */
    @Override
    public boolean addItem(T_ITEM item) {
      // check for vetos before doing the change
      try {
        fireVetoableChange(PageableCollection.EVENT_ITEM_ADD, null, item);
      } catch (PropertyVetoException e) {
        return false;
      }

      try {
        getBackingCollection().add(item);
      } catch (UnsupportedOperationException e) {
        throw new RuntimeException("Please check if you did provide a modifyable collection. Found collection type: " + getBackingCollection().getClass(), e);
      }

      // synchronize the optionally existing filtered and sorted row object list.
      if (filteredAndSortedObjects != null) {
        filteredAndSortedObjects.add(item);
      }

      doRegisterAddedItem(item);
      return true;
    };

    @Override
    public void registerAddedItem(T_ITEM item) {
      if (!getBackingCollection().contains(item)) {
        throw new RuntimeException("The new item is not part of the backing collection.\n\tPlease use either addItem() or add the item manually to your collection before calling registerAddedItem().");
      }
      // force recalculation of cached derived information.
      clearCaches();
      doRegisterAddedItem(item);
    }

    @Override
    public void registerUpdatedItem(T_ITEM item, boolean isUpdated) {
      // No veto event will be fired here, because the item update is not under control of this
      // collection.

      // a modification of a new item should not lead to a double-listing within the updated list too.
      if (isUpdated && modifications.getAddedItems().contains(item)) {
        return;
      }

      boolean wasUpdated = modifications.getUpdatedItems().contains(item);
      if (wasUpdated != isUpdated) {
        modifications.registerUpdatedItem(item, isUpdated);
        firePropertyChange(PageableCollection.EVENT_ITEM_UPDATE, wasUpdated, isUpdated);
      }
    };

    @Override
    public boolean removeSelectedItems() {
      Selection<T_ITEM> items = selectionHandler.getSelection();
      // nothing to remove if nothing is selected.
      if (items.isEmpty()) {
        return true;
      }

      // check for vetos before doing the change
      try {
        fireVetoableChange(PageableCollection.EVENT_REMOVE_SELECTION, items, null);
      } catch (PropertyVetoException e) {
        return false;
      }

      // Deselect all currently selected items because they will be deleted.
      SelectionHandlerUtil.forceSelectAll(selectionHandler, false);

      // Get the set of already removed items. It will be extended by this delete operation.
      Set<T_ITEM> removedItems = new HashSet<T_ITEM>(IterableUtil.asCollection(modifications.getRemovedItems()));
      for (T_ITEM i : items) {
        // remove the items from the in-memory item list(s).
        getBackingCollection().remove(i);
        if (filteredAndSortedObjects != null) {
          filteredAndSortedObjects.remove(i);
        }

        // Removed new items disappear without a trace. They are not part of the removed items.
        if (modifications.getAddedItems().contains(i)) {
          modifications.unregisterAddedItem(i);
        } else {
          removedItems.add(i);
        }
      }
      modifications.setRemovedItems(new ItemSetSelection<T_ITEM>(removedItems));
      firePropertyChange(PageableCollection.EVENT_REMOVE_SELECTION, items, null);
      return true;
    }

    @Override
    public void registerRemovedItems(Iterable<T_ITEM> items) {
      for (T_ITEM i : items) {
        if (modifications.getAddedItems().contains(i)) {
          // Removed new items disappear without a trace. They are not part of the removed items.
          modifications.unregisterAddedItem(i);
        } else {
          // Get the set of already removed items. It will be extended by this delete operation.
          Set<T_ITEM> removedItems = new HashSet<T_ITEM>(IterableUtil.asCollection(modifications.getRemovedItems()));
          removedItems.add(i);
          modifications.setRemovedItems(new ItemSetSelection<T_ITEM>(removedItems));
        }
        // handle optionally existing cached information:
        if (filteredAndSortedObjects != null) {
          filteredAndSortedObjects.remove(i);
        }
      }
      // all removed items must disappear from any selection.
      selectionHandler.select(false, items);
      Selection<T_ITEM> removedItemsSelection = new ItemSetSelection<T_ITEM>(new HashSet<T_ITEM>(ListUtil.toList(items)));
      firePropertyChange(PageableCollection.EVENT_REMOVE_SELECTION, removedItemsSelection, null);
    }

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

    private void doRegisterAddedItem(T_ITEM item) {
      modifications.registerAddedItem(item);
      InMemCollectionBase.this.firePropertyChange(PageableCollection.EVENT_ITEM_ADD, null, item);
    }
  }

}
