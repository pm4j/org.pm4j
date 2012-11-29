package org.pm4j.core.pm.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.pageable.inmem.PageableInMemCollectionImpl;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmPager2;
import org.pm4j.core.pm.PmTable.RowSelectMode;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmTableGenericRow2;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.annotation.PmTableCfg2;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.changehandler.ChangeSetHandler;
import org.pm4j.core.pm.impl.changehandler.ChangeSetHandler.ChangeKind;
import org.pm4j.core.pm.impl.changehandler.ChangeSetHandlerImpl;
import org.pm4j.core.pm.pageable.PageableCollection;
import org.pm4j.core.pm.pageable2.InMemPmQueryEvaluator;
import org.pm4j.core.pm.pageable2.PmTable2Util;

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
public class PmTableImpl2
        <T_ROW_PM extends PmBean<T_ROW_BEAN>, T_ROW_BEAN>
        extends PmDataInputBase
        implements PmTable2<T_ROW_PM> {

  private static final Log LOG = LogFactory.getLog(PmTableImpl2.class);

  /** The content this table is based on. */
  private PageableCollection2<T_ROW_PM> pmPageableCollection;

 /** Defines the row-selection behavior. */
  private SelectMode rowSelectMode;

  /**
   * The number of rows per page. If it is <code>null</code> the statically defined number of rows will be used.
   */
  private Integer numOfPageRows;

  /** Handles the changed state of the table. */
  private final ChangeSetHandlerImpl<T_ROW_PM> pmChangeSetHandler = new ChangeSetHandlerImpl<T_ROW_PM>(this);

  /** The set of decorators for various table change kinds. */
  private Map<TableChange, PmCommandDecoratorSetImpl> pmChangeDecoratorMap = Collections.emptyMap();

  /** Listens for selection changes and handles the table relates logic. */
  private TableSelectionChangeListener pmTableSelectionChangeListener = new TableSelectionChangeListener();

  /** Listens for filter changes and handles the table relates logic. */
  private TableFilterChangeListener pmTableFilterChangeListener = new TableFilterChangeListener();

  /**
   * Creates an empty table.
   * <p>
   * The table may be connected to some data source by calling {@link #setPmPageableCollection(PageableCollection)}.
   *
   * @param pmParent The presentation model context for this table.
   */
  public PmTableImpl2(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  public List<PmTableCol> getColumnPms() {
    return PmUtil.getPmChildrenOfType(this, PmTableCol.class);
  }

  @Deprecated
  @Override
  public final List<PmTableCol> getColumns() {
    return getColumnPms();
  }

  @Override
  public List<PmTableGenericRow2<T_ROW_PM>> getGenericRowPms() {
    List<PmTableGenericRow2<T_ROW_PM>> genericRows = null;

    // XXX olaf: The optimized version will have an event synchronized attribute.
    if (genericRows == null) {
      List<T_ROW_PM> rows = getRowPms();
      genericRows = new ArrayList<PmTableGenericRow2<T_ROW_PM>>(rows.size());
      for (T_ROW_PM r : rows) {
        genericRows.add(new PmTableGenericRowImpl2<T_ROW_PM>(this, r));
      }
    }

    return genericRows;
  }

  @Override
  public List<T_ROW_PM> getRowPms() {
    return getPmPageableCollection().getItemsOnPage();
  }

  @Deprecated
  @Override
  public final List<T_ROW_PM> getRows() {
    return getRowPms();
  }

  /**
   * Gets called whenever a new (transient) row gets added to the table.
   * Maintains the corresponding changed state for this table.
   *
   * @param newRowPm The new row.
   */
  protected void onAddNewRow(T_ROW_PM newRowPm) {
    pmChangeSetHandler.registerChange(ChangeKind.ADD, newRowPm);
  }

  /**
   * Gets called whenever a row gets deleted from the table.
   * Maintains the corresponding changed state for this table.
   *
   * @param newRowPm The new row.
   */
  protected void onDeleteRow(T_ROW_PM deletedRow) {
    PmMessageUtil.clearSubTreeMessages(deletedRow);
    getPmPageableCollection().getSelectionHandler().select(false, deletedRow);
    pmChangeSetHandler.registerChange(ChangeKind.DELETE, deletedRow);
    if (deletedRow instanceof PmBean) {
      BeanPmCacheUtil.removeBeanPm(this, (PmBean<?>)deletedRow);
    }
  }

  @Override
  public long getTotalNumOfPmRows() {
    return getPmPageableCollection().getNumOfItems();
  }

  @Deprecated
  @Override
  public final int getTotalNumOfRows() {
    return (int)getTotalNumOfPmRows();
  }

  public SelectMode getPmRowSelectMode() {
    if (rowSelectMode == null) {
      PmTableCfg2 cfg = AnnotationUtil.findAnnotation(this, PmTableCfg2.class);
      rowSelectMode = (cfg != null &&
                       cfg.rowSelectMode() != SelectMode.DEFAULT)
          ? cfg.rowSelectMode()
          // TODO: add to PmDefaults.
          : SelectMode.NO_SELECTION;
    }

    return rowSelectMode;
  }

  /** @deprecated Please use {@link #getPmRowSelectMode()} */
  public final SelectMode getRowSelectMode() {
    return getPmRowSelectMode();
  }

  /**
   * Adjusts the row selection mode.<br>
   * Should be called very early within the livecycle of the table.
   * The implementation does currently not fire any change events
   * sif this method gets called.
   *
   * @param rowSelectMode The {@link RowSelectMode} to be used by this table.
   */
  public void setPmRowSelectMode(SelectMode rowSelectMode) {
    this.rowSelectMode = rowSelectMode;
    if (pmPageableCollection != null) {
      pmPageableCollection.getSelectionHandler().setSelectMode(rowSelectMode);
    }
  }

  /** @deprecated Please use {@link #setPmRowSelectMode(SelectMode)} */
  public final void setRowSelectMode(SelectMode rowSelectMode) {
    setPmRowSelectMode(rowSelectMode);
  }


  @Override
  public int getNumOfPageRowPms() {
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

  @Deprecated
  @Override
  public final int getNumOfPageRows() {
    return getNumOfPageRowPms();
  }

  /**
   * @param numOfPageRows
   *          The number of rows per page. <br>
   *          If it is <code>null</code> the statically defined number of rows
   *          will be used.
   */
  public void setNumOfPageRowPms(Integer numOfPageRows) {
    this.numOfPageRows = numOfPageRows;
    if (pmPageableCollection != null) {
      pmPageableCollection.setPageSize(numOfPageRows);
    }
  }

  /** @deprecated Please use {@link #setNumOfPageRowPms(Integer)} */
  public final void setNumOfPageRows(Integer numOfPageRows) {
    setNumOfPageRowPms(numOfPageRows);
  }

  @Override
  protected boolean isPmReadonlyImpl() {
    return super.isPmReadonlyImpl() || getPmParent().isPmReadonly();
  }

  @Override
  public SelectionHandler<T_ROW_PM> getPmSelectionHandler() {
    return getPmPageableCollection().getSelectionHandler();
  }

  /**
   * This method provides access to a selection handler that uses the beans behind
   * the row PMs.
   * <p>
   * This allows to select/deselect rows based on a set of beans.
   *
   * @return the bean selection handler.
   */
  public SelectionHandler<T_ROW_BEAN> getPmBeanSelectionHandler() {
    return getPmPageableCollection().getBeanSelectionHandler();
  }

  @Override
  public T_ROW_PM getCurrentRowPm() {
    if (getPmRowSelectMode() == SelectMode.SINGLE) {
      Selection<T_ROW_PM> selection = getPmSelectionHandler().getSelection();
      if (selection.getSize() == 1) {
        return selection.iterator().next();
      }
    }

    return null;
  }

  /**
   * Provides the bean behind the current row PM.<br>
   * See {@link #getCurrentRowPm()}.
   *
   * @return the bean behind the currently active row PM or <code>null</code>.
   */
  public T_ROW_BEAN getCurrentRowPmBean() {
    T_ROW_PM rowPm = getCurrentRowPm();
    return (rowPm != null)
        ? rowPm.getPmBean()
        : null;
  }

  @Override
  public QueryParams getPmQueryParams() {
    return getPmPageableCollection().getQueryParams();
  }

  @Override
  public QueryOptions getPmQueryOptions() {
    return getPmPageableCollection().getQueryOptions();
  }

  @Override
  public void addPmDecorator(PmCommandDecorator decorator, TableChange... changes) {
    if (pmChangeDecoratorMap.isEmpty()) {
      pmChangeDecoratorMap = new HashMap<PmTable2.TableChange, PmCommandDecoratorSetImpl>();
    }

    TableChange[] changesToConsider = changes.length == 0 ? TableChange.values() : changes;
    for (TableChange c : changesToConsider) {
      PmCommandDecoratorSetImpl set = pmChangeDecoratorMap.get(c);
      if (set == null) {
        set = new PmCommandDecoratorSetImpl();
        pmChangeDecoratorMap.put(c, set);
      }
      set.addDecorator(decorator);

      // XXX olaf: check for a better solution:
      if (c == TableChange.PAGE && getPmPager() != null) {
        getPmPager().addPageChangeDecorator(decorator);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<PmCommandDecorator> getPmDecorators(TableChange change) {
    PmCommandDecoratorSetImpl set = pmChangeDecoratorMap.get(change);
    return (set != null) ? set.getDecorators() : Collections.EMPTY_LIST;
  }

  @Override
  public ChangeSetHandler<T_ROW_PM> getPmChangeSetHandler() {
    return pmChangeSetHandler;
  }

  @Override
  public final void updatePmTable(UpdateAspect... clearOptions) {
    UpdateAspect[] toProcess = clearOptions.length == 0
                      ? UpdateAspect.values()
                      : clearOptions;

    getPmPageableCollection().clearCaches();

    for (UpdateAspect o : toProcess) {
      clearPmAspectImpl(o);
    }
  }

  /**
   * Implements the table subclass sepecific update operation.
   * <p>
   * Should not be called directly. Please use {@link #updatePmTable(org.pm4j.core.pm.PmTable2.ClearAspect...)}
   * to trigger an update.
   *
   * @param clearAspect
   */
  protected void clearPmAspectImpl(UpdateAspect clearAspect) {
    switch (clearAspect) {
      case CLEAR_SELECTION:
        getPmSelectionHandler().selectAll(false);
        break;
      case CLEAR_SORT_ORDER:
        getPmQueryParams().setSortOrder(getPmQueryOptions().getDefaultSortOrder());
        break;
      case CLEAR_CHANGES:
        PmValidationApi.clearInvalidValuesOfSubtree(this);
        pmChangeSetHandler.clearChanges();
        break;
      case CLEAR_USER_FILTER:
        // User filters can't be cleared on this level. More detailed implementations
        // may implement user defined filters that may be cleared.
        break;
      default: throw new PmRuntimeException(this, "Unknown clear aspect: " + clearAspect);
    }
  }

  // -- helper methods --

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected boolean isPmValueChangedImpl() {
    return pmChangeSetHandler.isChanged() || super.isPmValueChangedImpl();
  }

  @Override
  protected void setPmValueChangedImpl(boolean changed) {
    super.setPmValueChangedImpl(changed);
    if (changed == false) {
      for (PmDataInput p : pmChangeSetHandler.getChangedItems(ChangeKind.ADD, ChangeKind.UPDATE)) {
        p.setPmValueChanged(false);
      }
      pmChangeSetHandler.clearChanges();
    }
  }

  /**
   * Validates the changed row items only.
   */
  @Override
  public void pmValidate() {
    for (PmObject itemPm : new ArrayList<PmObject>(pmChangeSetHandler.getChangedItems(ChangeKind.ADD, ChangeKind.UPDATE))) {
      if (itemPm instanceof PmDataInput) {
        PmValidationApi.validateSubTree((PmDataInput)itemPm);
      }
    }
  }


  /**
   * @return The {@link PageableCollection} that handles the table data to display.
   */
  public final PageableCollection2<T_ROW_PM> getPmPageableCollection() {
    zz_ensurePmInitialization();
    if (pmPageableCollection == null) {
      _setPageableCollection(getPmPageableCollectionImpl());
    }
    return pmPageableCollection;
  }

  /**
   * Gets called whenever the internal collection is <code>null</code> and
   * {@link #getPmPageableCollection()} gets called.
   *
   * @return The collection to use. Never <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  protected PageableCollection2<T_ROW_PM> getPmPageableCollectionImpl() {
    QueryOptions qoptions = PmTable2Util.makeQueryOptionsForInMemoryTable(this);
    return new PageableInMemCollectionImpl<T_ROW_PM>(
        new InMemPmQueryEvaluator<T_ROW_PM>(this),
        (Collection<T_ROW_PM>)Collections.EMPTY_LIST,
        qoptions,
        null);
  }

  /**
   * A post processing method that allow to apply some default settings to a new pageable collection.
   * <p>
   * Gets called whenever a new {@link #pmPageableCollection} gets assigned:
   * <ul>
   *  <li>by calling {@link #getPmPageableCollectionImpl()} or </li>
   *  <li>by a call to {@link #setPmPageableCollection(PageableCollection, boolean, ValueChangeKind)}.</li>
   * </ul>
   * The default settings applied in this base implementation are:
   * <ul>
   *  <li>Number of page rows and multi-select setting.</li>
   *  <li>The reference of the (optional) pager to the collection.</li>
   * </ul>
   * Sub classes may override this method to extend this logic.
   *
   * @param pageableCollection the collection to initialize.
   */
  protected void initPmPageableCollection(PageableCollection2<T_ROW_PM> pageableCollection) {
    pageableCollection.getSelectionHandler().setSelectMode(getPmRowSelectMode());
    pageableCollection.setPageSize(getNumOfPageRowPms());

    // XXX olaf: Check - is redundant to the change listener within Pager!
    if (getPmPager() != null) {
      getPmPager().setPageableCollection(pageableCollection);
    }
  }

  /**
   * Defines the data set to be presented by the table.
   *
   * @param pageable
   *          the data set to present. If it is <code>null</code> an empty
   *          collection will be created internally by the next {@link #getPmPageableCollection()} call.
   */
  public void setPmPageableCollection(PageableCollection2<T_ROW_PM> pageable) {
    setPmPageableCollection(pageable, true, ValueChangeKind.VALUE);
  }

  /**
   * @param pageable
   *          the data set to present. If it is <code>null</code> an empty
   *          collection will be created internally by the next {@link #getPmPageableCollection()} call.
   * @param preserveSettings Defines if the currently selected items and filter definition should be preserved.
   * @return <code>true</code> if the data set was new.
   */
  public void setPmPageableCollection(PageableCollection2<T_ROW_PM> pageable, boolean preserveSettings, ValueChangeKind valueChangeKind) {
    Selection<T_ROW_PM> selection = null;

    if (preserveSettings) {
      if (pmPageableCollection != null) {
        selection = pmPageableCollection.getSelectionHandler().getSelection();
      }
    }
    else {
      BeanPmCacheUtil.clearBeanPmCache(this);
    }

    _setPageableCollection(pageable);
    PmEventApi.firePmEventIfInitialized(this, PmEvent.VALUE_CHANGE, valueChangeKind);

    // re-apply the settings to preserve
    if (preserveSettings && selection != null) {
      // ensure that the internal field is set, even it was just reset to null.
      getPmPageableCollection();
      pmPageableCollection.getSelectionHandler().setSelection(selection);
    }
  }

  /**
   * @return A pager that may be used to navigate through the table.<br>
   *         May return <code>null</code> if there is no pager defined for this
   *         table.
   */
  public PmPager2 getPmPager() {
    return null;
  }

  // XXX olaf: combine with init method.
  private void _setPageableCollection(PageableCollection2<T_ROW_PM> pc) {
    if (this.pmPageableCollection != pc) {
      SelectionHandler<T_ROW_PM> selectionHandler = pc.getSelectionHandler();
      selectionHandler.addPropertyAndVetoableListener(SelectionHandler.PROP_SELECTION, pmTableSelectionChangeListener);
      pc.getQueryParams().addPropertyAndVetoableListener(QueryParams.PROP_EFFECTIVE_FILTER, pmTableFilterChangeListener);

      if (pmPageableCollection != null) {
        pmPageableCollection.getSelectionHandler().removePropertyAndVetoableListener(SelectionHandler.PROP_SELECTION, pmTableSelectionChangeListener);
        pmPageableCollection.getQueryParams().removePropertyAndVetoableListener(QueryParams.PROP_EFFECTIVE_FILTER, pmTableFilterChangeListener);
      }
    }

    this.pmPageableCollection = pc;
    if (pmPageableCollection != null) {
      initPmPageableCollection(pmPageableCollection);
    }
  }

  // -- support classes --

  /**
   * Base implementation for a table specific pager.
   */
  public class Pager extends PmPagerImpl2 {

    public Pager(PmTable2<?> parentTablePm) {
      super(parentTablePm);
    }

    @Override
    protected void onPmInit() {
      PmEventApi.addPmEventListener(PmTableImpl2.this, PmEvent.VALUE_CHANGE, new PmEventListener() {
        @Override
        public void handleEvent(PmEvent event) {
          setPageableCollection(PmTableImpl2.this.getPmPageableCollection());
        }
      });
    }
  }

  /**
   * A property change listener that forwards event calls to registered {@link TableChange#SELECTION} decorators.
   * <p>
   * TODO olaf: should use an intermediate command to allow the standard logic for confirmed changes...
   */
  class TableSelectionChangeListener implements PropertyAndVetoableChangeListener {

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      for (PmCommandDecorator d : getPmDecorators(TableChange.SELECTION)) {
        if (!d.beforeDo(null)) {
          String msg = "Decorator prevents selection change: " + d;
          LOG.debug(msg);
          throw new PropertyVetoException(msg, evt);
        }
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      for (PmCommandDecorator d : getPmDecorators(TableChange.SELECTION)) {
        d.afterDo(null);
      }
    }
  }

  /**
   * A property change listener that forwards event calls to registered {@link TableChange#FILTER} decorators.
   * <p>
   * TODO olaf: should use an intermediate command to allow the standard logic for confirmed changes...
   */
  class TableFilterChangeListener implements PropertyAndVetoableChangeListener {

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      for (PmCommandDecorator d : getPmDecorators(TableChange.FILTER)) {
        if (!d.beforeDo(null)) {
          String msg = "Decorator prevents filter change: " + d;
          LOG.debug(msg);
          throw new PropertyVetoException(msg, evt);
        }
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      PageableCollectionUtil2.ensureCurrentPageInRange(getPmPageableCollection());

      for (PmCommandDecorator d : getPmDecorators(TableChange.FILTER)) {
        d.afterDo(null);
      }
    }
  }

}
