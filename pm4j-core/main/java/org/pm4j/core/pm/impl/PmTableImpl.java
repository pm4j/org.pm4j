package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pm4j.common.util.InvertingComparator;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmDataInput;
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

  /**
   * The number of rows per page. If it is <code>null</code> the statically defined number of rows will be used.
   */
  private Integer numOfPageRows;

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
    if (deletedRow instanceof PmBean) {
      BeanPmCacheUtil.removeBeanPm(this, (PmBean<?>)deletedRow);
    }
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

  /**
   * Adjusts the row selection mode.<br>
   * Should be called very early within the livecycle of the table.
   * The implementation does currently not fire any change events
   * sif this method gets called.
   *
   * @param rowSelectMode The {@link RowSelectMode} to be used by this table.
   */
  public void setRowSelectMode(RowSelectMode rowSelectMode) {
    this.rowSelectMode = rowSelectMode;
    if (pageableCollection != null) {
      pageableCollection.setMultiSelect(rowSelectMode == RowSelectMode.MULTI);
    }
  }

  @Override
  public int getNumOfPageRows() {
    return (numOfPageRows != null)
        ? numOfPageRows
        : getOwnMetaDataWithoutPmInitCall().numOfPageRows;
  }

  /**
   * @param numOfPageRows
   *          The number of rows per page. <br>
   *          If it is <code>null</code> the statically defined number of rows
   *          will be used.
   */
  public void setNumOfPageRows(Integer numOfPageRows) {
    this.numOfPageRows = numOfPageRows;
    if (pageableCollection != null) {
      pageableCollection.setPageSize(numOfPageRows);
    }
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

  void onSortOrderChange(PmTableCol sortColumnPm) {
    PmTableCol newSortCol = (sortColumnPm.getSortOrderAttr().getValue() != PmSortOrder.NEUTRAL)
                ? sortColumnPm
                : null;
    // mark current sort order as invalid and fire a value change event.
    sortOrderSelection.sortBy(newSortCol);
  }

  class SortOrderSelection {
    /** The column to sort the rows by. */
    PmTableCol sortCol = null;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void sortBy(PmTableCol sortCol) {
      if (this.sortCol != null &&
          this.sortCol != sortCol) {
        // XXX olaf: The backing value gets modified to prevent call back notifications.
        //           Can be done better when the event source implementation is done ...
        ((PmAttrEnumImpl<PmSortOrder>)this.sortCol.getSortOrderAttr()).setBackingValue(PmSortOrder.NEUTRAL);
      }
      this.sortCol = sortCol;

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

// TODO olaf: find a way to do this without breaking the top-down initialization.
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

  // TODO olaf: move two methode to a value change util: set individual and sub-tree changed states
  void setPmValueChanged(boolean newChangedState) {
    changedStateRegistry.clearChangedItems();
  }

  /**
   * Validates the changed row items only.
   */
  @Override
  public void pmValidate() {
    for (PmObject itemPm : new ArrayList<PmObject>(changedStateRegistry.getChangedItems())) {
      if (itemPm instanceof PmDataInput) {
        ((PmDataInput)itemPm).pmValidate();
      }
    }
  }


  /**
   * @return The container that handles the table data to display.
   */
  public PageableCollection<T_ROW_ELEMENT_PM> getPageableCollection() {
    zz_ensurePmInitialization();
    if (pageableCollection == null) {
      pageableCollection = getPageableCollectionImpl();
      // XXX olaf: Risk of side effects to other users referencing the given collection.
      //           Find a better solution...
      //           Generates potentially an initialization problem if called in constructor
      pageableCollection.setMultiSelect(getRowSelectMode() == RowSelectMode.MULTI);
      pageableCollection.setPageSize(getNumOfPageRows());
      pager.setPmBean(pageableCollection);
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

    if (!preseveSettings) {
      BeanPmCacheUtil.clearBeanPmCache(this);
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

    pager.setPmBean(pageableCollection);
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
