package org.pm4j.common.pageable.querybased.pagequery;

import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandlerBase;
import org.pm4j.common.util.CloneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a {@link PageQueryCollection} based selection.
 * <p>
 * Because of the unlimited nature of the query the selection can't reference
 * all selected items directly.
 * <p>
 * Two basic selection types are implemented:
 * <ul>
 * <li>{@link PageQueryItemIdSelection}: contains the id's of the selected items.</li>
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
public abstract class PageQuerySelectionHandler<T_ITEM, T_ID> extends SelectionHandlerBase<T_ITEM> {

  private static final Logger LOG = LoggerFactory.getLogger(PageQuerySelectionHandler.class);

  private final PageQueryService<T_ITEM, T_ID> service;
  private final PageQueryItemIdSelection<T_ITEM, T_ID> emptySelection;
  private QuerySelectionWithClickedIds<T_ITEM, T_ID> currentSelection;


  @SuppressWarnings("unchecked")
  public PageQuerySelectionHandler(PageQueryService<T_ITEM, T_ID> service) {
    assert service != null;

    this.service = service;
    this.emptySelection = new PageQueryItemIdSelection<T_ITEM, T_ID>(service, getQueryOptions().getIdAttribute(), getQueryParams(), Collections.EMPTY_LIST, false);
    this.currentSelection = emptySelection;
  }

  @Override
  public boolean select(boolean select, T_ITEM item) {
    T_ID id = service.getIdForItem(item);
    Set<T_ID> idSet = getClickedIdSet();
    boolean inverse = isInverse();

    if (select && !inverse) {
      switch (getSelectMode()) {
      case SINGLE:
        idSet.clear();
        break;
      case MULTI:
        break;
      default:
        throw new RuntimeException("Selection for select mode '" + getSelectMode() + "' is not supported.");
      }
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
    Set<T_ID> idSet = getClickedIdSet();
    for (T_ITEM i : items) {
      if (select ^ isInverse()) {
        idSet.add(service.getIdForItem(i));
      } else {
        idSet.remove(service.getIdForItem(i));
      }
    }

    checkMultiSelectResult(idSet);
    return setSelection(idSet);
  }

  @Override
  public boolean selectAll(boolean select) {
    if (select && getSelectMode() != SelectMode.MULTI) {
      throw new RuntimeException("Select all for current select mode is not supported: " + getSelectMode());
    }

    Selection<T_ITEM> s = select
          // All items selected: all query items do match and there are no de-selecting clicks.
          ? new InvertedSelection<T_ITEM, T_ID>(service, getQueryParams(), emptySelection)
          : emptySelection;

    if (!setSelection(s)) {
      return false;
    }

    return true;
  }

  @Override
  public boolean invertSelection() {
    if (getSelectMode() != SelectMode.MULTI) {
      throw new RuntimeException("Invert selection is not supported for select mode: " + getSelectMode());
    }

    return setSelection(isInverse()
        ? new PageQueryItemIdSelection<T_ITEM, T_ID>(service, getQueryOptions().getIdAttribute(),  getQueryParams(), currentSelection.getClickedIds().getIds(), false)
        : new InvertedSelection<T_ITEM, T_ID>(service, getQueryParams(), currentSelection));
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    ensureSelectionState();
    return currentSelection;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean setSelection(final Selection<T_ITEM> selectionArg) {
    Selection<T_ITEM> selection;
    // in case of an empty selection we may get a type without 'clicked ids' that's handled here:
    if (selectionArg instanceof QuerySelectionWithClickedIds) {
      selection = selectionArg;
    } else {
      if (selectionArg.isEmpty()) {
        selection = emptySelection;
      } else {
        throw new IllegalArgumentException("Can handle only selections of type QuerySelectionWithClickedIds. Found type: " + selectionArg.getClass());
      }
    }

    Selection<T_ITEM> oldSelection = this.currentSelection;
    QuerySelectionWithClickedIds<T_ITEM, T_ID> newSelection = (QuerySelectionWithClickedIds<T_ITEM, T_ID>) selection;

    // check for noop:
    if (oldSelection.isEmpty() && newSelection.isEmpty()) {
      return true;
    }

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

  @Override
  public Selection<T_ITEM> getAllItemsSelection() {
    return new PageQueryAllItemsSelection<T_ITEM, T_ID>(service, getQueryParams());
  }

  /**
   * Sub classes provide here the query that represents all selectable items.
   *
   * @return the query parameter set. Never <code>null</code>.
   */
  protected abstract QueryParams getQueryParams();

  /**
   * Sub classes provide here the query options.
   *
   * @return the query options set. Never <code>null</code>.
   */
  protected abstract QueryOptions getQueryOptions();

  /**
   * @return <code>true</code> if the actual query is a kind of {@link InvertedSelection}.
   */
  protected final boolean isInverse() {
    return (currentSelection instanceof InvertedSelection);
  }

  /** @return the set of clicked id's */
  private Set<T_ID> getClickedIdSet() {
    return new HashSet<T_ID>(currentSelection.getClickedIds().getIds());
  }

  /**
   * Creates a new current selection and fires a property change event.
   *
   * @param selectedIds the new set of selected id's. In case if an inverted selection: the new set of de-selected id's.
   */
  private boolean setSelection(Set<T_ID> selectedIds) {
    PageQueryItemIdSelection<T_ITEM, T_ID> idSelection = selectedIds.isEmpty()
                  ? emptySelection
                  : new PageQueryItemIdSelection<T_ITEM, T_ID>(service, getQueryOptions().getIdAttribute(), getQueryParams(), selectedIds, true);

    return setSelection(isInverse()
                  ? new InvertedSelection<T_ITEM, T_ID>(service, getQueryParams(), idSelection)
                  : idSelection);
  }

  /** Base class for query based selections that consider a set of clicked ID's. */
  public static abstract class QuerySelectionWithClickedIds<T_ITEM, T_ID> extends PageQuerySelectionBase<T_ITEM, T_ID> {

    private static final long serialVersionUID = 1L;

    public QuerySelectionWithClickedIds(QueryService<T_ITEM, T_ID> service) {
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

  /**
   * A selection that is based on a query that identifies all items.<br>
   * It may have also a set of de-selected item-identifiers.
   */
  static class InvertedSelection<T_ITEM, T_ID> extends QuerySelectionWithClickedIds<T_ITEM, T_ID> {

    private static final long serialVersionUID = 1L;
    private final QueryParams query;
    private final QuerySelectionWithClickedIds<T_ITEM, T_ID> baseSelection;
    transient private Long size;
    /** The query fetch block size. */
    private int iteratorBlockSizeHint = 20;

    public InvertedSelection(PageQueryService<T_ITEM, T_ID> service, QueryParams query, QuerySelectionWithClickedIds<T_ITEM, T_ID> baseSelection) {
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
        size = getPageableQueryService().getItemCount(query) - baseSelection.getSize();
      }
      return size;
    }

    @Override
    public boolean contains(T_ITEM item) {
      return ! baseSelection.contains(item);
    }

    @Override
    public Iterator<T_ITEM> iterator() {
      return new PageQueryItemIteratorBase<T_ITEM>(iteratorBlockSizeHint) {
        @Override
        protected boolean isItemSelected(T_ITEM item) {
          return !baseSelection.contains(item);
        }

        @Override
        protected List<T_ITEM> getItems(long startIdx, int blockSize) {
          return getPageableQueryService().getItems(query, startIdx, blockSize);
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

    /** Type access helper */
    protected PageQueryService<T_ITEM, T_ID> getPageableQueryService() {
      return (PageQueryService<T_ITEM, T_ID>) getService();
    }

  }

}
