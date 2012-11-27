package org.pm4j.common.pageable.querybased;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandlerBase;
import org.pm4j.core.util.lang.CloneUtil;

/**
 * Handler for a {@link PageableQueryCollection} based selection.
 * <p>
 * Because of the unlimited nature of the query the selection can't reference
 * all selected items directly.
 * <p>
 * Two basic selection types are implemented:
 * <ul>
 * <li>{@link ItemIdSelection}: contains the id's of the selected items.</li>
 * <li>{@link InvertedSelection}: contains the {@link QueryParams} to represent
 * the 'ALL' selection and a set of de-selected id's.
 * </ul>
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 *          the handled item type.
 * @param <T_ID>
 *          type of the related item identifier.
 */
public class PageableQuerySelectionHandler<T_ITEM, T_ID extends Serializable> extends SelectionHandlerBase<T_ITEM> {

  private static final Log LOG = LogFactory.getLog(PageableQuerySelectionHandler.class);

  private final PageableQueryService<T_ITEM, T_ID> service;
  private final QueryParams query;
  private final ItemIdSelection<T_ITEM, T_ID> emptySelection;
  private ItemIdSelection<T_ITEM, T_ID> idSelection;
  private SelectionBase<T_ITEM, T_ID> currentSelection;
  private boolean inverse;

  @SuppressWarnings("unchecked")
  public PageableQuerySelectionHandler(PageableQueryService<T_ITEM, T_ID> service, QueryParams query) {
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
  public boolean invertSelection() {
    if (getSelectMode() != SelectMode.MULTI) {
      throw new RuntimeException("Invert selection is not supported for select mode: " + getSelectMode());
    }

    return setSelection(inverse
        ? new ItemIdSelection<T_ITEM, T_ID>(service, currentSelection.getClickedIds().getIds())
        : new InvertedSelection<T_ITEM, T_ID>(service, query, currentSelection));
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    return currentSelection;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean setSelection(Selection<T_ITEM> selection) {
    Selection<T_ITEM> oldSelection = this.currentSelection;
    Selection<T_ITEM> newSelection = selection;

    try {
      fireVetoableChange(PROP_SELECTION, oldSelection, newSelection);
      // XXX olaf: check of that can be doene safely...
      this.currentSelection = (SelectionBase<T_ITEM, T_ID>) newSelection;
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

  public static abstract class SelectionBase<T_ITEM, T_ID extends Serializable> extends PageableQuerySelectionBase<T_ITEM, T_ID> {

    private static final long serialVersionUID = 1L;

    public SelectionBase(PageableQueryService<T_ITEM, T_ID> service) {
      super(service);
    }

    /**
     * Provides the manually clicked id's. Depending on the selection type (normal or inverted)
     * these are the selected or de-selected item id's.
     *
     * @return the set of clicked id's.
     */
    public abstract ClickedIds<T_ID> getClickedIds();

  }


  static class ItemIdSelection<T_ITEM, T_ID extends Serializable> extends SelectionBase<T_ITEM, T_ID> {
    private static final long serialVersionUID = 1L;

    private final Collection<T_ID> ids;

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

    /** Block size has currently no effect on this iterator implementation. This may be changed in the future. */
    @Override
    public void setIteratorBlockSizeHint(int readBlockSize) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T_BEAN> Selection<T_BEAN> getBeanSelection() {
      return (Selection<T_BEAN>)this;
    }

    protected Collection<T_ID> getSelectedOrDeselectedIds() {
      return ids;
    }

    @Override
    public ClickedIds<T_ID> getClickedIds() {
      return new ClickedIds<T_ID>(ids, false);
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
        // TODO olaf: not yet optimized to read in blocks
        return getService().getItemForId(id);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    }
  }

  static class InvertedSelection<T_ITEM, T_ID extends Serializable> extends SelectionBase<T_ITEM, T_ID> {

    private static final long serialVersionUID = 1L;
    private final QueryParams query;
    private final SelectionBase<T_ITEM, T_ID> baseSelection;
    transient private Long size;
    /** The query fetch block size. */
    private int iteratorBlockSizeHint = 20;

    public InvertedSelection(PageableQueryService<T_ITEM, T_ID> service, QueryParams query, SelectionBase<T_ITEM, T_ID> baseSelection) {
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
      return new PageableItemIteratorBase<T_ITEM>(iteratorBlockSizeHint) {
        @Override
        protected boolean isItemSelected(T_ITEM item) {
          return !baseSelection.contains(item);
        }

        @Override
        protected List<T_ITEM> getItems(long startIdx, int blockSize) {
          return getService().getItems(query, startIdx, blockSize);
        }
      };
    }

    @Override
    public void setIteratorBlockSizeHint(int iteratorBlockSizeHint) {
      this.iteratorBlockSizeHint = iteratorBlockSizeHint;
    }

    public ClickedIds<T_ID> getClickedIds() {
      return new ClickedIds<T_ID>(baseSelection.getClickedIds().getIds(), true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T_BEAN> Selection<T_BEAN> getBeanSelection() {
      return (Selection<T_BEAN>)this;
    }
  }

}
