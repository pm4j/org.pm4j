package org.pm4j.common.pageable.querybased;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.query.Query;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandlerBase;
import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.util.lang.CloneUtil;

public class PageableQuerySelectionHandler<T_ITEM, T_ID extends Serializable> extends SelectionHandlerBase<T_ITEM> {

  private static final Log LOG = LogFactory.getLog(PageableQuerySelectionHandler.class);

  private final PageableQueryService<T_ITEM, T_ID> service;
  private final Query query;
  private final ItemIdSelection<T_ITEM, T_ID> emptySelection;
  private ItemIdSelection<T_ITEM, T_ID> idSelection;
  private Selection<T_ITEM> currentSelection;
  private boolean inverse;

  @SuppressWarnings("unchecked")
  public PageableQuerySelectionHandler(PageableQueryService<T_ITEM, T_ID> service, Query query) {
    assert service != null;
    assert query != null;

    this.service = service;
    this.query = query;
    this.emptySelection = new ItemIdSelection<T_ITEM, T_ID>(service, Collections.EMPTY_LIST);
    this.idSelection = emptySelection;
    this.currentSelection = idSelection;
  }

  @Override
  public boolean select(boolean select, T_ITEM item) {
    T_ID id = service.getIdForItem(item);
    Set<T_ID> idSet = getModifyableIdSet();

    if (select && ! inverse) {
      beforeAddSingleItemSelection(idSet);
    }

    if (select ^ inverse) {
      idSet.add(id);
    } else {
      idSet.remove(id);
    }

    return setSelection(idSet);
  }

  @Override
  public boolean select(boolean select, Iterable<T_ITEM> items) {
    Set<T_ID> idSet = getModifyableIdSet();
    for (T_ITEM i : items) {
      if (select ^ inverse) {
        idSet.add(service.getIdForItem(i));
      } else {
        idSet.remove(service.getIdForItem(i));
      }
    }

    checkMultiSelectResult(idSet);
    return setSelection(idSet);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean selectAll(boolean select) {
    if (select && getSelectMode() != SelectMode.MULTI) {
      throw new RuntimeException("Select all for current select mode is not supported: " + getSelectMode());
    }

    boolean oldInverse = inverse;
    inverse = select;
    if (!setSelection(Collections.EMPTY_SET)) {
      inverse = oldInverse;
      return false;
    }

    return true;
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    return currentSelection;
  }

  @Override
  public boolean setSelection(Selection<T_ITEM> selection) {
    Selection<T_ITEM> oldSelection = this.currentSelection;
    Selection<T_ITEM> newSelection = selection;

    try {
      fireVetoableChange(PROP_SELECTION, oldSelection, newSelection);
      this.currentSelection = newSelection;
      firePropertyChange(PROP_SELECTION, oldSelection, newSelection);
      return true;
    } catch (PropertyVetoException e) {
      LOG.debug("Selection change rejected because of a property change veto.", e);
      return false;
    }
  }

  private Set<T_ID> getModifyableIdSet() {
    return new HashSet<T_ID>(idSelection.getSelectedOrDeselectedIds());
  }

  /**
   * Creates a new current selection and fires a property change event.
   *
   * @param selectedIds the new set of selected id's. In case if an inverted selection: the new set of de-selected id's.
   */
  private boolean setSelection(Set<T_ID> selectedIds) {
    idSelection = selectedIds.isEmpty()
                  ? emptySelection
                  : new ItemIdSelection<T_ITEM, T_ID>(service, selectedIds);

    return setSelection(inverse
                  ? new InvertedSelection<T_ITEM, T_ID>(service, query, idSelection)
                  : idSelection);
  }

  /** A selection base class that supports serializable selections. */
  static abstract class SerializeableSelectionBase<T_ITEM, T_ID extends Serializable> implements Selection<T_ITEM>, Serializable {
    private static final long serialVersionUID = 1L;

    /** The service provider may be <code>null</code> in case of non-serializeable selections. */
    private PageableQueryService.SerializeableServiceProvider<T_ITEM, T_ID> serviceProvider;
    transient private PageableQueryService<T_ITEM, T_ID> service;

    public SerializeableSelectionBase(PageableQueryService<T_ITEM, T_ID> service) {
      assert service != null;

      this.service = service;
      this.serviceProvider = service.getSerializeableServiceProvider();
    }

    protected PageableQueryService<T_ITEM, T_ID> getService() {
      if (service == null) {
        if (serviceProvider != null) {
          service = serviceProvider.getQueryService();
        }

        if (service == null) {
          throw new RuntimeException("Your PageableQueryService does not support serialization of selections.\n" +
              "Please implement PageableQueryService.getSerializeableServiceProvider() to get serializeable selections.");
        }
      }
      return service;
    }
  }

  static class ItemIdSelection<T_ITEM, T_ID extends Serializable> extends SerializeableSelectionBase<T_ITEM, T_ID> {
    private static final long serialVersionUID = 1L;

    private Collection<T_ID> ids;

    @SuppressWarnings("unchecked")
    public ItemIdSelection(PageableQueryService<T_ITEM, T_ID> service, Collection<T_ID> ids) {
      super(service);
      this.ids = (ids != null) ? Collections.unmodifiableCollection(ids) : Collections.EMPTY_LIST;
    }

    @Override
    public long getSize() {
      return ids.size();
    }

    @Override
    public boolean contains(T_ITEM item) {
      return ids.contains(getService().getIdForItem(item));
    }

    @Override
    public Iterator<T_ITEM> iterator() {
      return new ItemIterator();
    }

    protected Collection<T_ID> getSelectedOrDeselectedIds() {
      return ids;
    }

    class ItemIterator implements Iterator<T_ITEM> {
      private final Iterator<T_ID> idIterator = ids.iterator();

      @Override
      public boolean hasNext() {
        return idIterator.hasNext();
      }

      @Override
      public T_ITEM next() {
        T_ID id = idIterator.next();
        return getService().getItemForId(id);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    }
  }

  static class InvertedSelection<T_ITEM, T_ID extends Serializable> extends SerializeableSelectionBase<T_ITEM, T_ID> {

    private static final long serialVersionUID = 1L;
    private final Query query;
    private final Selection<T_ITEM> baseSelection;
    transient private Long size;

    public InvertedSelection(PageableQueryService<T_ITEM, T_ID> service, Query query, Selection<T_ITEM> baseSelection) {
      super(service);
      assert query != null;
      assert baseSelection != null;

      // query must be cloned to protect the selection against future changes.
      // the life times of the query and the selection may be very different.
      this.query = CloneUtil.clone(query);
      this.baseSelection = baseSelection;
    }

    @Override
    public long getSize() {
      if (size == null) {
        size = getService().getItemCount(query) - baseSelection.getSize();
      }
      return size;
    }

    @Override
    public boolean contains(T_ITEM item) {
      return ! baseSelection.contains(item);
    }

    @Override
    public Iterator<T_ITEM> iterator() {
      return new ItemIterator();
    }

    class ItemIterator implements Iterator<T_ITEM> {

      private long idx;
      private T_ITEM item;

      public ItemIterator() {
        doNext();
      }

      @Override
      public boolean hasNext() {
        return item != null;
      }

      @Override
      public T_ITEM next() {
        T_ITEM result = item;
        doNext();
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private void doNext() {
        boolean nextFound = false;
        do {
          item = ListUtil.listToItemOrNull(getService().getItems(query, idx, 1));
          if (item == null) {
            idx = -1;
            item = null;
            return;
          }

          ++idx;
          nextFound = !baseSelection.contains(item);
        }
        while(!nextFound);
      }

    }
  }

}
