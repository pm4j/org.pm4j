package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pm4j.common.util.InvertingComparator;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmTableGenericRow;
import org.pm4j.core.pm.PmTableRow;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.pageable.PageableCollection;
import org.pm4j.core.pm.pageable.PageableCollection.Filter;
import org.pm4j.core.pm.pageable.PageableListImpl;
import org.pm4j.core.pm.pageable.PmPager;
import org.pm4j.core.pm.pageable.PmPagerImpl;

/**
 * A table that presents the content of a set of {@link PmElement}s.
 * <p>
 * This class provides the model for visual table components ({@link PmTableCol}, {@link PmTableRow}, {@link PmPager}).
 * <p>
 * The table data related logic is provided by a {@link PageableCollection}.
 * This collection supports the logic for
 * <ul>
 * <li>pagination (see {@link PageableCollection#setCurrentPageIdx(int)} etc.).</li>
 * <li>row selection (see {@link PageableCollection#select(Object)} etc.).</li>
 * <li>sorting (see {@link PageableCollection#sortItems(Comparator)}).</li>
 * <li>filtering (see TODO: )</li>
 * </ul>.
 * <p>
 *
 * @author olaf boede
 */
public class PmTableImpl
        <T_ROW_ELEMENT_PM extends PmElement>
        extends PmDataInputBase
        implements PmTable<T_ROW_ELEMENT_PM> {

  /** The content this table is based on. */
  private PageableCollection<T_ROW_ELEMENT_PM> pageableCollection;

  /** Container for the sort specification and the sorted items. */
  private SortOrderSelection sortOrderSelection;

  /** Defines the row-selection behavior. */
  private RowSelectMode rowSelectMode;

  /** Handles the changed state of the table. */
  private final ChangedChildStateRegistry changedStateRegistry = new ChangedChildStateRegistry(this);


  /** A pager that may be used to navigate through the table. */
  public final PmPagerImpl<T_ROW_ELEMENT_PM> pager = new PmPagerImpl<T_ROW_ELEMENT_PM>(this) {
    @Override
    protected void onPmInit() {
      PmEventApi.addPmEventListener(PmTableImpl.this, PmEvent.VALUE_CHANGE, new PmEventListener() {
        @Override
        public void handleEvent(PmEvent event) {
          setPmBean(getPageableCollection());
        }
      });
    }
  };

  /**
   * Creates an empty table.
   * <p>
   * The table may be connected to some data source by calling {@link #setPageableCollection(PageableCollection)}.
   *
   * @param pmParent The presentation model context for this table.
   * @param pageableItems Provides the items to be displayed by this table.
   */
  public PmTableImpl(PmObject pmParent) {
    this(pmParent, null);
  }

  /**
   * Creates a table that is connected to a data source (a {@link PageableCollection}).
   *
   * @param pmParent The presentation model context for this table.
   * @param pageableItems Provides the items to be displayed by this table.
   */
  public PmTableImpl(PmObject pmParent, PageableCollection<T_ROW_ELEMENT_PM> pageableItems) {
    super(pmParent);
    if (pageableItems != null) {
      setPageableCollection(pageableItems, false);
    }
  }

  @Override
  public List<PmTableCol> getColumns() {
    return zz_getPmColumns();
  }

  @Override
  public List<PmTableGenericRow<T_ROW_ELEMENT_PM>> getGenericRows() {
    List<PmTableGenericRow<T_ROW_ELEMENT_PM>> genericRows = null;

    // XXX olaf: The optimized version will have an event synchronized attribute.
    if (genericRows == null) {
      List<T_ROW_ELEMENT_PM> rows = getRows();
      genericRows = new ArrayList<PmTableGenericRow<T_ROW_ELEMENT_PM>>(rows.size());
      for (T_ROW_ELEMENT_PM r : rows) {
        genericRows.add(new PmTableGenericRowImpl<T_ROW_ELEMENT_PM>(this, r));
      }
    }

    return genericRows;
  }

  @Override
  public List<T_ROW_ELEMENT_PM> getRows() {
    return getPageableCollection().getItemsOnPage();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public List<T_ROW_ELEMENT_PM> getRowsWithChanges() {
    return new ArrayList<T_ROW_ELEMENT_PM>((Collection)changedStateRegistry.getChangedItems());
  }

  /**
   * Gets called whenever a new (transient) row gets added to the table.
   * Maintains the corresponding changed state for this table.
   *
   * @param newRowPm The new row.
   */
  protected void onAddNewRow(T_ROW_ELEMENT_PM newRowPm) {
    changedStateRegistry.onAddNewItem(newRowPm);
  }

  /**
   * Gets called whenever a row gets deleted from the table.
   * Maintains the corresponding changed state for this table.
   *
   * @param newRowPm The new row.
   */
  protected void onDeleteRow(T_ROW_ELEMENT_PM deletedRow) {
    changedStateRegistry.onDeleteItem(deletedRow);
  }

  @Override
  public int getTotalNumOfRows() {
    return getPageableCollection().getNumOfItems();
  }

  public RowSelectMode getRowSelectMode() {
    return rowSelectMode != null
              ? rowSelectMode
              : getOwnMetaDataWithoutPmInitCall().rowSelectMode;
  }

  // XXX olaf: Really required to have that?
  public void setRowSelectMode(RowSelectMode rowSelectMode) {
    this.rowSelectMode = rowSelectMode;
    if (pageableCollection != null) {
      pageableCollection.setMultiSelect(rowSelectMode == RowSelectMode.MULTI);
    }
  }

  @Override
  public int getNumOfPageRows() {
    return getOwnMetaDataWithoutPmInitCall().numOfPageRows;
  }

  // XXX olaf: should disappear. getRowSelectMode should be enough.
  @Override
  public boolean isMultiSelect() {
    return getRowSelectMode() == RowSelectMode.MULTI;
  }

  @Override
  protected boolean isPmReadonlyImpl() {
    if (super.isPmReadonlyImpl()) {
      return true;
    }
    else {
      PmObject ctxt = getPmParent();
      return (ctxt != null &&
              ctxt.isPmReadonly()) ||
             !isPmEnabled();
    }
  }

  @Override
  public T_ROW_ELEMENT_PM getSelectedRow() {
    Collection<T_ROW_ELEMENT_PM> items = getSelectedRows();
    return (!items.isEmpty())
            ? items.iterator().next()
            : null;
  }

  @Override
  public Collection<T_ROW_ELEMENT_PM> getSelectedRows() {
    return getPageableCollection().getSelectedItems();
  }

  // -- row sort order support --

  class SortOrderSelection implements PmEventListener {
// FIXME olaf: Check sort attribute event listener initialization.
//             Currently there is a conflict with the too simple single phase initialization.
//    /** The column to sort the rows by. */
//    PmTableCol sortCol = null;
//
//    public SortOrderSelection(Collection<PmTableCol> columns) {
//      for (PmTableCol c : columns) {
//        PmAttrEnum<PmSortOrder> a = c.getSortOrderAttr();
//
//        // Whenever a the sort order attribute of a column changes, the sorted row
//        // set needs to be updated.
//        PmEventApi.addPmEventListener(a, PmEvent.VALUE_CHANGE, this);
//
//        // Identify the initial sort order (if there is one).
//        if (a.getValue() != PmSortOrder.NEUTRAL) {
//          sortCol = c;
//        }
//      }
//    }

    @Override
    public void handleEvent(PmEvent event) {
      @SuppressWarnings("unchecked")
      PmAttrEnum<PmSortOrder> a = (PmAttrEnum<PmSortOrder>)event.pm;
      PmTableCol sortCol = a.getValue() != PmSortOrder.NEUTRAL
                  ? (PmTableCol) a.getPmParent()
                  : null;

      // mark current sort order as invalid and fire a value change event.
      sortBy(sortCol);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void sortBy(PmTableCol sortCol) {
      Comparator<?> comparator = null;
      if (sortCol != null) {
        // The explicitly configured comparator will be used.
        // Otherwise the generic row comparator.
        comparator = sortCol.getRowSortComparator();
        if (comparator == null) {
          PmSortOrder order = sortCol.getSortOrderAttr().getValue();

          // prevent unnecessary non-sorting sort operations.
          if (order != null && order != PmSortOrder.NEUTRAL) {
            comparator = new RowPmComparator<T_ROW_ELEMENT_PM>(sortCol);
            // sort the PM items.
            getPageableCollection().sortItems(comparator);
            PmEventApi.firePmEvent(PmTableImpl.this, PmEvent.VALUE_CHANGE);
            return;
          }
        }
        else {
          if (sortCol.getSortOrderAttr().getValue() == PmSortOrder.DESC) {
            comparator = new InvertingComparator(comparator);
          }
        }

      }

      // sort based on a backing items related comparator.
      getPageableCollection().sortBackingItems(comparator);
      PmEventApi.firePmEvent(PmTableImpl.this, PmEvent.VALUE_CHANGE);
    }
  }

  /**
   * Compares the column specific cells of two rows based on the sort order definition of the given column.
   * <p>
   * May be used for tables that hold PMs for all rows.
   * <p>
   * ATTENTION: Can't be used for tables that told only PMs for the rows on the current page.
   *
   * @param <T_ROW_ELEMENT_PM> The row PM type.
   */
  static class RowPmComparator<T_ROW_ELEMENT_PM extends PmElement> implements Comparator<T_ROW_ELEMENT_PM> {
    private final PmTableCol sortColumn;

    public RowPmComparator(PmTableCol sortColumn) {
      assert sortColumn != null;
      this.sortColumn = sortColumn;
    }

    @Override
    public int compare(T_ROW_ELEMENT_PM o1, T_ROW_ELEMENT_PM o2) {
      PmObject cellPm1 = zz_getPmRowCellForColumn(o1, sortColumn);
      PmObject cellPm2 = zz_getPmRowCellForColumn(o2, sortColumn);

      switch (sortColumn.getSortOrderAttr().getValue()) {
        case ASC:  return cellPm1.compareTo(cellPm2);
        case DESC: return - cellPm1.compareTo(cellPm2);
        default:   return 0;
      }
    }
  }

  // -- helper methods --

  @Override
  protected void onPmInit() {
    super.onPmInit();

// FIXME olaf: find a way to do this without breaking the top-down initialization.
//    if (columns == null) {
//      columns = zz_getPmColumns();
//
//      // Initial column position setup.
//      for (int i=0; i<columns.size(); ++i) {
//        PmTableCol c = columns.get(i);
//        c.getColPosAttr().setValue(i);
//      }
//    }

      sortOrderSelection = new SortOrderSelection();
      // register listener for selection change events
      PmEventApi.addPmEventListener(this, PmEvent.SELECTION_CHANGE, new PmEventListener() {

        @Override
        public void handleEvent(PmEvent event) {
          PmTableImpl.this.onPmSelectionChange(event);
        }
      });

  }

  private static PmObject zz_getPmRowCellForColumn(PmElement rowElement, PmTableCol column) {
    PmObject pm = PmUtil.findChildPm(rowElement, column.getPmName());
    if (pm == null) {
      throw new PmRuntimeException(rowElement, "No table row item found for column '" + column.getPmName());
    }
    return pm;
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean isPmValueChanged() {
    return changedStateRegistry.isAChangeRegistered();
  }

  /**
   * @return The container that handles the table data to display.
   */
  public PageableCollection<T_ROW_ELEMENT_PM> getPageableCollection() {
    zz_ensurePmInitialization();
    if (pageableCollection == null) {
      pageableCollection = getPageableCollectionImpl();
    }
    return pageableCollection;
  }

  /**
   * Gets called whenever the internal collection is <code>null</code> and
   * {@link #getPageableCollection()} gets called.
   *
   * @return The collection to use. Never <code>null</code>.
   */
  protected PageableCollection<T_ROW_ELEMENT_PM> getPageableCollectionImpl() {
    return new PageableListImpl<T_ROW_ELEMENT_PM>(getNumOfPageRows(), getRowSelectMode() == RowSelectMode.MULTI);
  }

  /**
   * Sets an empty {@link #pageableCollection} if the given parameter is <code>null</code>.
   *
   * @param pageable The new data set to present.
   * @param preseveSettings Defines if the currently selected items and filter definition should be preserved.
   * @return <code>true</code> if the data set was new.
   */
  public void setPageableCollection(PageableCollection<T_ROW_ELEMENT_PM> pageable, boolean preseveSettings) {
    Collection<T_ROW_ELEMENT_PM> selectedItems = Collections.emptyList();
    Filter<?> filter = null;
    if (pageableCollection != null && preseveSettings) {
      selectedItems = pageableCollection.getSelectedItems();
      filter = pageableCollection.getBackingItemFilter();
    }

    pageableCollection = pageable;

    if (pageable != null) {
      // XXX olaf: Risk of side effects to other users referencing the given collection.
      //           Find a better solution...
      //           Generates potentially an initialization problem if called in constructor
      pageableCollection.setMultiSelect(getRowSelectMode() == RowSelectMode.MULTI);
      pageableCollection.setPageSize(getNumOfPageRows());
    }

    PmEventApi.firePmEventIfInitialized(this, PmEvent.VALUE_CHANGE);

    // re-apply the settings to preserve
    if (preseveSettings) {
      // ensure that the internal field is set, even it was just reset to null.
      getPageableCollection();

      for (T_ROW_ELEMENT_PM r : selectedItems) {
        pageableCollection.select(r);
      }
      pageableCollection.setBackingItemFilter(filter);
    }

  }


  /**
   * @return A pager that may be used to navigate through the table.
   */
  public PmPager getPager() {
    return pager;
  }

  /**
   * Is called whenever an event with the flag {@link PmEvent#SELECTION_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmSelectionChange(PmEvent event) {
  }

  // -- metadata handling --

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

//  @Override
//  protected void initMetaData(PmObjectBase.MetaData metaData) {
//    super.initMetaData(metaData);
//    MetaData myMetaData = (MetaData) metaData;

// TODO olaf: Not yet implemented.
//    PmTableCfg annotation = AnnotationUtil.findAnnotation(this, PmTableCfg.class);
//    if (annotation != null) {
//      if (annotation.rowSelectMode() != RowSelectMode.DEFAULT) {
//        myMetaData.rowSelectMode = annotation.rowSelectMode();
//      }
//      if (annotation.numOfPageRows() > 0) {
//        myMetaData.numOfPageRows = annotation.numOfPageRows();
//      }
//    }
//  }

  protected static class MetaData extends PmObjectBase.MetaData {
    private RowSelectMode rowSelectMode = RowSelectMode.SINGLE;
    private int numOfPageRows = 10;
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

}
