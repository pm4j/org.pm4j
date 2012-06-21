package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmPager;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmTableGenericRow;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.filter.Filter;
import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.MultiFilter;
import org.pm4j.core.pm.pageable.PageableCollection;
import org.pm4j.core.pm.pageable.PageableListImpl;
import org.pm4j.core.util.reflection.ClassUtil;

/**
 * A table that presents the content of a set of {@link PmElement}s.
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

  /** The set of decorators for various table change kinds. */
  private Map<TableChange, PmCommandDecoratorSetImpl> changeDecoratorMap = Collections.emptyMap();

  /** The set of named table row filters. */
  private MultiFilter rowFilter = new MultiFilter();

  private final Comparator<Object> NO_COMPARATOR = new Comparator<Object>() {
    @Override public int compare(Object o1, Object o2) { throw new PmRuntimeException(PmTableImpl.this, "The internal NO_COMPARATOR should not be used."); }
  };

  private Comparator<?> initialBeanSortComparator = NO_COMPARATOR;


  /**
   * Creates an empty table.
   * <p>
   * The table may be connected to some data source by calling {@link #setPageableCollection(PageableCollection)}.
   *
   * @param pmParent The presentation model context for this table.
   */
  public PmTableImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  public List<PmTableCol> getColumns() {
    return PmUtil.getPmChildrenOfType(this, PmTableCol.class);
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

  @Override
  public List<FilterByDefinition> getFilterByDefinitions() {
    List<FilterByDefinition> list = new ArrayList<FilterByDefinition>();
    for (PmTableCol c : getColumns()) {
      Collection<FilterByDefinition> colFilters = c.getFilterByDefinitions();
      if (colFilters != null) {
        list.addAll(colFilters);
      }
    }
    return list;
  }



  @Override
  public Filter getFilter(String filterId) {
    return rowFilter.getFilter(filterId);
  }

  @Override
  public boolean setFilter(final String filterId, final Filter filter) {
    PmCommandImpl cmd = new PmCommandImpl(this) {
      @Override
      protected void doItImpl() {
        rowFilter.setFilter(filterId, filter);
        getPageableCollection().setItemFilter(rowFilter);
      }
      @Override
      protected BEFORE_DO getBeforeDoStrategy() {
        return BEFORE_DO.DO_NOTHING;
      }
    };

    for (PmCommandDecorator d : getDecorators(TableChange.FILTER)) {
      cmd.addCommandDecorator(d);
    }

    return cmd.doIt().getCommandState() == CommandState.EXECUTED;
  }

  /**
   * Defines a fix filter that will not be cleared when the table gets re-initialized.
   * <p>
   * Can be called already within the constructor.
   *
   * @param filterId
   * @param filter
   */
  public void setFixFilter(final String filterId, final Filter filter) {
    rowFilter.setFixFilter(filterId, filter);
    if (pageableCollection != null) {
      pageableCollection.setItemFilter(rowFilter);
    }
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
    PmMessageUtil.clearSubTreeMessages(deletedRow);
    getPageableCollection().select(deletedRow, false);
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
    if (rowSelectMode == null) {
      PmTableCfg cfg = AnnotationUtil.findAnnotation(this, PmTableCfg.class);
      rowSelectMode = (cfg != null &&
                       cfg.rowSelectMode() != RowSelectMode.DEFAULT)
          ? cfg.rowSelectMode()
          // TODO: add to PmDefaults.
          : RowSelectMode.NO_SELECTION;
    }

    return rowSelectMode;
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
    if (numOfPageRows == null) {
      PmTableCfg cfg = AnnotationUtil.findAnnotation(this, PmTableCfg.class);
      numOfPageRows = (cfg != null &&
                       cfg.numOfPageRows() > 0)
          ? cfg.numOfPageRows()
          // TODO: add to PmDefaults.
          : 10;
    }
    return numOfPageRows;
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

  @Override
  public void addDecorator(PmCommandDecorator decorator, TableChange... changes) {
    if (changeDecoratorMap.isEmpty()) {
      changeDecoratorMap = new HashMap<PmTable.TableChange, PmCommandDecoratorSetImpl>();
    }

    TableChange[] changesToConsider = changes.length == 0 ? TableChange.values() : changes;
    for (TableChange c : changesToConsider) {
      PmCommandDecoratorSetImpl set = changeDecoratorMap.get(c);
      if (set == null) {
        set = new PmCommandDecoratorSetImpl();
        changeDecoratorMap.put(c, set);
      }
      set.addDecorator(decorator);

      // XXX olaf: check for a better solution:
      if (c == TableChange.PAGE && getPager() != null) {
        getPager().addPageChangeDecorator(decorator);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<PmCommandDecorator> getDecorators(TableChange change) {
    PmCommandDecoratorSetImpl set = changeDecoratorMap.get(change);
    return (set != null) ? set.getDecorators() : Collections.EMPTY_LIST;
  }

  // -- row sort order support --

  /**
   * Sorts that table by the given column and sort order.
   * <p>
   * It the given <code>sortColumnPm</code> or <code>sortOrder</code> is
   * <code>null</code> or {@link PmSortOrder#NEUTRAL} the table switches to a
   * not sorted state.
   *
   * @param sortColumnPm
   *          The column to sort by.
   * @param sortOrder
   *          The sort order to apply.
   */
  @SuppressWarnings("unchecked")
  protected void sortBy(PmTableCol sortColumnPm, PmSortOrder sortOrder) {
    zz_ensurePmInitialization();

    PmSortOrder newSortOrder = sortOrder != null
        ? sortOrder
        : PmSortOrder.NEUTRAL;
    PmTableCol newSortCol = (newSortOrder != PmSortOrder.NEUTRAL)
        ? sortColumnPm
        : null;

    if (sortColumnPm != null && sortColumnPm.getSortOrderAttr().getValue() != newSortOrder) {
      // XXX olaf: a quick hack to prevent an event loop.
      ((PmAttrBase<?, PmSortOrder>)sortColumnPm.getSortOrderAttr()).setBackingValue(newSortOrder);
    }

    // mark current sort order as invalid and fire a value change event.
    sortOrderSelection.sortBy(newSortCol);
  }


  protected static class SortOrderSpec {
    PmTableCol sortByColumn;
    PmSortOrder sortOrder;

    public SortOrderSpec(PmTableCol sortByColumn, PmSortOrder sortOrder) {
      this.sortByColumn = sortByColumn;
      this.sortOrder = sortOrder;
    }
  }

  class SortOrderSelection {
    /** The column to sort the rows by. */
    PmTableCol sortCol = null;

    private void sortBy(PmTableCol sortCol) {
      // Sort column changed: Reset the sort order of the current sort column.
      if (this.sortCol != null &&
          this.sortCol != sortCol) {

        // XXX olaf: The backing value gets modified to prevent call back notifications.
        //           Can be done better when the event source implementation is done ...
        ((PmAttrEnumImpl<PmSortOrder>)this.sortCol.getSortOrderAttr()).setBackingValue(PmSortOrder.NEUTRAL);
      }

      this.sortCol = sortCol;

      if (sortCol != null) {
        PmSortOrder order = sortCol.getSortOrderAttr().getValue();

        // prevent unnecessary non-sorting sort operations.
        if (order != null && order != PmSortOrder.NEUTRAL) {
          Comparator<?> comparator = new RowPmComparator<T_ROW_ELEMENT_PM>(sortCol);
          // sort the PM items.
          getPageableCollection().sortItems(comparator);
          PmEventApi.firePmEventIfInitialized(PmTableImpl.this, PmEvent.VALUE_CHANGE, ValueChangeKind.SORT_ORDER);
          return;
        }
      }

      // Reset the sort order.
      // Can be postponed to the next getPageableColleciton call if the collection is not yet created.
      if (pageableCollection != null) {
        getPageableCollection().sortBackingItems(null);
        PmEventApi.firePmEventIfInitialized(PmTableImpl.this, PmEvent.VALUE_CHANGE, ValueChangeKind.SORT_ORDER);
      }
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
      PmObject cellPm1 = PmTableUtil.getRowCellForTableCol(sortColumn, o1);
      PmObject cellPm2 = PmTableUtil.getRowCellForTableCol(sortColumn, o2);

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
  public final PageableCollection<T_ROW_ELEMENT_PM> getPageableCollection() {
    zz_ensurePmInitialization();
    if (pageableCollection == null) {
      pageableCollection = getPageableCollectionImpl();
      initPageableCollection(pageableCollection, false);
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
   * A post processing method that allow to apply some default settings to a new pageable collection.
   * <p>
   * Gets called whenever a new {@link #pageableCollection} gets assigned:
   * <ul>
   *  <li>by calling {@link #getPageableCollectionImpl()} or </li>
   *  <li>by a call to {@link #setPageableCollection(PageableCollection, boolean, ValueChangeKind)}.</li>
   * </ul>
   * The default settings applied in this base implementation are:
   * <ul>
   *  <li>Number of page rows and multi-select setting.</li>
   *  <li>The reference of the (optional) pager to the collection.</li>
   *  <li>The current filter and sort order settings.</li>
   * </ul>
   * Sub classes may override this method to extend this logic.
   *
   * @param pageableCollection The collection to initialize.
   */
  protected void initPageableCollection(PageableCollection<T_ROW_ELEMENT_PM> pageableCollection, boolean preserveSettings) {
    // XXX olaf: Risk of side effects to other users referencing the given collection.
    //           Find a better solution...
    //           Generates potentially an initialization problem if called in constructor
    pageableCollection.setMultiSelect(getRowSelectMode() == RowSelectMode.MULTI);
    pageableCollection.setPageSize(getNumOfPageRows());

    // The row-filter needs to be re-applied to the pageable collection.
    if (!rowFilter.isEmpty()) {
      pageableCollection.setItemFilter(rowFilter);
    }

    if (!preserveSettings) {
      applyDefaultSortOrder();
    }

    // XXX olaf: Check - is redundant to the change listener within Pager!
    if (getPager() != null) {
      getPager().setPageableCollection(pageableCollection);
    }
  }

  // FIXME olaf: a test...
  public void setPageableCollection(PageableCollection<T_ROW_ELEMENT_PM> pageable, boolean preseveSettings) {
    setPageableCollection(pageable, preseveSettings, ValueChangeKind.UNKNOWN);
  }

  /**
   * Sets an empty {@link #pageableCollection} if the given parameter is <code>null</code>.
   *
   * @param pageable The new data set to present.
   * @param preserveSettings Defines if the currently selected items and filter definition should be preserved.
   * @return <code>true</code> if the data set was new.
   */
  public void setPageableCollection(PageableCollection<T_ROW_ELEMENT_PM> pageable, boolean preserveSettings, ValueChangeKind valueChangeKind) {
    Collection<T_ROW_ELEMENT_PM> selectedItems = Collections.emptyList();

    if (preserveSettings) {
      if (pageableCollection != null) {
        selectedItems = pageableCollection.getSelectedItems();
      }
    }
    else {
      BeanPmCacheUtil.clearBeanPmCache(this);
      rowFilter.clear();
    }

    pageableCollection = pageable;

    if (pageableCollection != null) {
      initPageableCollection(pageableCollection, preserveSettings);
    }

    PmEventApi.firePmEventIfInitialized(this, PmEvent.VALUE_CHANGE, valueChangeKind);

    // re-apply the settings to preserve
    if (preserveSettings && !selectedItems.isEmpty()) {
      // ensure that the internal field is set, even it was just reset to null.
      getPageableCollection();

      for (T_ROW_ELEMENT_PM r : selectedItems) {
        pageableCollection.select(r, true);
      }
    }
  }

  /**
   * Provides the default sort order column.
   * <p>
   * The default implementation reads this information from the annotation {@link PmTableCfg#defaultSortCol()}.
   *
   * @return The default sort order or <code>null</code>.
   */
  protected SortOrderSpec getDefaultSortOrder() {
    PmTableCfg tableCfg = AnnotationUtil.findAnnotation(this, PmTableCfg.class);

    if (tableCfg != null &&
        StringUtils.isNotBlank(tableCfg.defaultSortCol()))  {
      // first part: Column name. Second part: optional 'asc'/'desc' order spec
      String[] strings = tableCfg.defaultSortCol().split(",");
      PmTableCol col = PmUtil.getPmChildOfType(this, strings[0], PmTableCol.class);
      if (strings.length == 1) {
        return new SortOrderSpec(col, PmSortOrder.ASC);
      }
      if (strings.length == 2) {
        if ("asc".equalsIgnoreCase(strings[1])) {
          return new SortOrderSpec(col, PmSortOrder.ASC);
        } else if ("desc".equalsIgnoreCase(strings[1])) {
          return new SortOrderSpec(col, PmSortOrder.DESC);
        } else {
          throw new PmRuntimeException(
              getPmParent(),
              "Unknown sort direction identifier '"
                  + strings[1]
                  + "' found in annotation attribute 'defaultSortCol'."
                  + "\n\tPlease specify 'asc', 'desc' or simply omit the comma separated sort direction identifier to get the default ('asc').");
        }
      } else {
        throw new PmRuntimeException(
            getPmParent(),
            "Invalid 'defaultSortCol' value '"
                + tableCfg.defaultSortCol()
                + "'. It may only have a single comma separator.");
      }
    }
    // no default sort spec
    return null;
  }

  /**
   * Defines a bean comparator that provides the initial table sort order.
   * <p>
   * The default implementation provides the comparator defined in {@link PmTableCfg#initialBeanSortComparator()}.
   *
   * @return The bean sort comparator. May be <code>null</code>.
   */
  Comparator<?> getInitialBeanSortComparator() {
    if (initialBeanSortComparator == NO_COMPARATOR) {
      PmTableCfg tableCfg = AnnotationUtil.findAnnotation(this, PmTableCfg.class);
      initialBeanSortComparator = (tableCfg != null && tableCfg.initialBeanSortComparator() != Comparator.class)
          ? (Comparator<?>) ClassUtil.newInstance(tableCfg.initialBeanSortComparator())
          : null;
    }
    return initialBeanSortComparator;
  }

  /**
   * Applies the initial- and column based sort order to the {@link PageableCollection}.
   */
  protected void applyDefaultSortOrder() {
    // get the initial sort order
    pageableCollection.setInitialBeanSortComparator(getInitialBeanSortComparator());

    // Switch back to the default sort order.
    SortOrderSpec s = getDefaultSortOrder();
    if (s != null) {
      sortBy(s.sortByColumn, s.sortOrder);
    }
    else {
      sortBy(null, null);
    }
  }

  /**
   * @return A pager that may be used to navigate through the table.<br>
   *         May return <code>null</code> if there is no pager defined for this
   *         table.
   */
  public PmPager getPager() {
    return null;
  }

  /**
   * Is called whenever an event with the flag {@link PmEvent#SELECTION_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmSelectionChange(PmEvent event) {
  }


  /**
   * Base implementation for a table specific pager.
   */
  public class Pager extends PmPagerImpl {

    public Pager(PmTable<?> parentTablePm) {
      super(parentTablePm);
    }

    @Override
    protected void onPmInit() {
      PmEventApi.addPmEventListener(PmTableImpl.this, PmEvent.VALUE_CHANGE, new PmEventListener() {
        @Override
        public void handleEvent(PmEvent event) {
          setPageableCollection(PmTableImpl.this.getPageableCollection());
        }
      });
    }
  }

  // -- metadata handling --

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

// TODO olaf: Not yet implemented.

//  @Override
//  protected void initMetaData(PmObjectBase.MetaData metaData) {
//    super.initMetaData(metaData);
//    MetaData myMetaData = (MetaData) metaData;

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

//  protected static class MetaData extends PmObjectBase.MetaData {
//    private RowSelectMode rowSelectMode = RowSelectMode.SINGLE;
//    private int numOfPageRows = 10;
//  }
//
//  private final MetaData getOwnMetaDataWithoutPmInitCall() {
//    return (MetaData) getPmMetaDataWithoutPmInitCall();
//  };
}
