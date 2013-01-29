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

import org.pm4j.common.pageable.ModificationHandler;
import org.pm4j.common.pageable.Modifications;
import org.pm4j.common.pageable.ModificationsImpl;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.selection.ItemSetSelection;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.selection.SelectionHandlerWithItemSet;
import org.pm4j.common.util.collection.IterableUtil;

/**
 * Implements a {@link PageableCollection} based on an {@link List} of items to
 * handle.
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 *          The type of items handled by this set.
 */
public class PageableInMemCollectionImpl<T_ITEM>
  extends PageableCollectionBase2<T_ITEM>
  implements PageableInMemCollection<T_ITEM>, PageableCollection2<T_ITEM> {

  /** The collection type specific selection handler. */
  private final SelectionHandler<T_ITEM> selectionHandler;

  /** Contains the not filtered set of items in their original sort order. */
  private Collection<T_ITEM>             originalObjects;
  /** The current set of filtered and sorted items. */
  private List<T_ITEM>                   objects;
  /** The currently active sort order comparator. */
  private Comparator<T_ITEM>             sortOrderComparator;

  private InMemQueryEvaluator<T_ITEM>    inMemQueryEvaluator;

  private final InMemModificationHandler modificationHandler;


  /** A listener gets called if a query property gets changed that affects the effective filter result. */
  private PropertyChangeListener changeFilterListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      objects = null;
    }
  };
  /** A listener gets called if a query property gets changed that affects the sort order of the items to show. */
  private PropertyChangeListener changeSortOrderListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      sortOrderComparator = null;
      objects = null;
    }
  };

  /**
   * @param objects
   *          the set of objects to iterate over.
   */
  public PageableInMemCollectionImpl(Collection<T_ITEM> objects) {
    this(new InMemQueryEvaluator<T_ITEM>(), objects, null, null);
  }

  /**
   * @param objects
   *          the set of objects to iterate over.
   * @param query
   *          defines the current sort order and filter restricions.
   */
  public PageableInMemCollectionImpl(InMemQueryEvaluator<T_ITEM> inMemQueryEvaluator, Collection<T_ITEM> objects, QueryOptions queryOptions, QueryParams query) {
    super(queryOptions, query);
    this.inMemQueryEvaluator = inMemQueryEvaluator;
    this.selectionHandler = new SelectionHandlerWithItemSet<T_ITEM>(this);
    this.originalObjects = (objects != null)
        ? objects
        : new ArrayList<T_ITEM>();

    // getQuery is used because the super ctor may have created it on the fly (in case of a null parameter)
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, changeFilterListener);
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, changeSortOrderListener);

    modificationHandler = new InMemModificationHandler();
  }

  @Override
  public List<T_ITEM> getItemsOnPage() {
    List<T_ITEM> objects = _getObjects();
    if (objects.isEmpty()) {
      return Collections.emptyList();
    }

    int first = PageableCollectionUtil2.getIdxOfFirstItemOnPageAsInt(this) - 1;
    int last = PageableCollectionUtil2.getIdxOfLastItemOnPageAsInt(this);

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
    objects = null;
  }

  @Override
  public void setSortOrderComparator(Comparator<T_ITEM> comparator) {
    if (comparator != this.sortOrderComparator) {
      sortOrderComparator = comparator;
      objects = null;
    }
  }

  private Comparator<T_ITEM> _getSortOrderComparator() {
    if (sortOrderComparator == null) {
      sortOrderComparator = inMemQueryEvaluator.getComparator(getQueryParams().getEffectiveSortOrder());
    }
    return sortOrderComparator;
  }

  private List<T_ITEM> _getObjects() {
    if (objects == null) {
      if (originalObjects.isEmpty() || !getQueryParams().isExecQuery()) {
        objects = Collections.emptyList();
      }
      else {
        List<T_ITEM> list = _filter(new ArrayList<T_ITEM>(originalObjects));
        Comparator<T_ITEM> comparator = _getSortOrderComparator();

        if (comparator != null) {
          Collections.sort(list, comparator);
        }

        objects = list;
      }

      // XXX olaf: just moves to the last possible page if necessary.
      // The user may want to stay on the page with his selected item.
      // We need to define strategies for application specific definitions.
      PageableCollectionUtil2.ensureCurrentPageInRange(this);
    }

    return objects;
  }

  /** Generates a list of filtered items based on the given list. */
  private List<T_ITEM> _filter(List<T_ITEM> unfilteredList) {
    FilterExpression filterExpression = getQueryParams().getFilterExpression();
    if (filterExpression == null) {
      return unfilteredList;
    }

    List<T_ITEM> filteredList = inMemQueryEvaluator.evaluateSubSet(unfilteredList, filterExpression);
    return filteredList;
  }


  class InMemModificationHandler implements ModificationHandler<T_ITEM> {

    private ModificationsImpl<T_ITEM> modifications = new ModificationsImpl<T_ITEM>();

    /**
     * Adds the item as the last one.
     */
    @Override
    public void addItem(T_ITEM item) {
      originalObjects.add(item);
      if (objects != null) {
        objects.add(item);
      }
      modifications.registerAddedItem(item);
      PageableInMemCollectionImpl.this.firePropertyChange(PageableCollection2.EVENT_ITEM_ADD, null, item);
    };

    @Override
    public void updateItem(T_ITEM item, boolean isUpdated) {
      boolean wasUpdated = modifications.getUpdatedItems().contains(item);
      modifications.registerUpdatedItem(item, isUpdated);
      PageableInMemCollectionImpl.this.firePropertyChange(PageableCollection2.EVENT_ITEM_UPDATE, wasUpdated, isUpdated);
    };

    @Override
    public boolean removeSelectedItems() {
      Selection<T_ITEM> items = selectionHandler.getSelection();
      if (items.getSize() == 0) {
        return true;
      }

      try {
        PageableInMemCollectionImpl.this.fireVetoableChange(PageableCollection2.EVENT_REMOVE_SELECTION, items, null);
      } catch (PropertyVetoException e) {
        return false;
      }

      SelectionHandlerUtil.forceSelectAll(selectionHandler, false);

      Set<T_ITEM> removedItems = new HashSet<T_ITEM>(IterableUtil.asCollection(modifications.getRemovedItems()));
      for (T_ITEM i : items) {
        originalObjects.remove(i);
        if (objects != null) {
          objects.remove(i);
        }
        removedItems.add(i);
      }
      modifications.setRemovedItems(new ItemSetSelection<T_ITEM>(removedItems));
      PageableInMemCollectionImpl.this.firePropertyChange(PageableCollection2.EVENT_REMOVE_SELECTION, items, null);
      return true;
    }

    @Override
    public void clearRegisteredModifications() {
      modifications = new ModificationsImpl<T_ITEM>();
    }

    @Override
    public Modifications<T_ITEM> getModifications() {
      return modifications;
    }
  }

}

