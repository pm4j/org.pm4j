package org.pm4j.core.pm.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.inmem.InMemCollection;
import org.pm4j.common.pageable.inmem.InMemCollectionBase;
import org.pm4j.common.pageable.inmem.InMemCollectionImpl;
import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.pageable.querybased.idquery.IdQueryCollectionImpl;
import org.pm4j.common.pageable.querybased.idquery.IdQueryService;
import org.pm4j.common.pageable.querybased.pagequery.PageQueryCollection;
import org.pm4j.common.pageable.querybased.pagequery.PageQueryService;
import org.pm4j.common.query.QueryOptionProvider;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.query.filter.FilterDefinitionFactory;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.common.util.reflection.GenericTypeUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.*;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.annotation.PmTableCfg.RowsToValidate;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.cache.CacheStrategyBase;
import org.pm4j.core.pm.impl.cache.CacheStrategyRequest;
import org.pm4j.core.pm.impl.pageable.PmBeanCollection;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Implementation for @{link PmTable}.
 * <p>
 * The table data related logic is provided by a {@link PageableCollection}.
 * This collection supports the logic for
 * <ul>
 * <li>pagination (see {@link PageableCollection#setPageIdx(long)} etc.).</li>
 * <li>row selection</li>
 * <li>sorting</li>
 * <li>filtering (see TODO: ...)</li>
 * </ul>.
 * <p>
 *
 * @author olaf boede
 */
public class PmTableImpl
        <T_ROW_PM extends PmBean<T_ROW_BEAN>, T_ROW_BEAN>
        extends PmObjectBase
        implements PmTable<T_ROW_PM> {

  /**
   * The default number of rows per page. Is used if no number of rows is specified
   * in {@link PmTableCfg#numOfPageRows()} or by calling {@link #setNumOfPageRowPms(Integer)}.
   */
  public static final int DEFAULT_NUM_OF_PAGE_ROW_PMS = 10;

  private static final Logger LOG = LoggerFactory.getLogger(PmTableImpl.class);

  /** The content this table is based on. */
  private PmBeanCollection<T_ROW_PM, T_ROW_BEAN> pmPageableCollection;

  /** Defines the row-selection behavior. */
  private SelectMode pmRowSelectMode;

  /**
   * The number of rows per page. If it is <code>null</code> the statically defined number of rows will be used.
   */
  private Integer numOfPageRowPms;

  /** The set of decorators for various table change kinds. */
  private Map<TableChange, PmCommandDecoratorSetImpl> pmChangeDecoratorMap = Collections.emptyMap();

  /** An event adapter that forwards collection events to table events. */
  private InternalPmTableEventAdapterForPageableCollection pageableCollectionEventAdapter;

  /** A cached reference to the selected master row. */
  /* package */ T_ROW_PM masterRowPm;

  /** The set of supported cache strategies. */
  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_IN_MEM_COLLECTION =
      MapUtil.makeFixHashMap(
        CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
        CacheMode.ON,       new CacheStrategyImMemCollectionReference("CACHE_TABLE_COLLECTION_LOCALLY"),
        CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_TABLE_COLLECTION_IN_REQUEST", "tc")
      );

  /** An optionally used cache for in-memory backing collections. */
  private Object pmInMemCollectionCache;

  /** An indicator that protects a getter logic based table against concurrent setter logic access. */
  boolean pmCollectionGetterLogicUsed;

  /**
   * Creates a table PM.
   *
   * @param pmParent The presentation model context for this table.
   */
  public PmTableImpl(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * This method gets called whenever the PM observes an initialization or reload event.
   * The default implementation refreshes all table aspects
   * (values, selection, sort order, user filters).
   * <p>
   * You may change that by overriding this method.
   * <p>
   * This method supports the common use case of a table that represents a some
   * content of a larger data bean. The larger bean is then usually the backing
   * bean of one of the table PM parents. If that bean gets exchanged, a value
   * change event will be propagated to the related PM sub-tree. See:
   * {@link PmBean#setPmBean(Object)}.
   *
   * @param event The event.
   */
  @Override
  protected void onPmDataExchangeEvent(PmEvent event) {
    super.onPmDataExchangeEvent(event);

    if (event.isReloadEvent()) {
      // A reload of handled data. Changes and selections are no longer valid.
      // But sort order and user filter stay as they are.
      updatePmTable(UpdateAspect.CLEAR_CHANGES, UpdateAspect.CLEAR_SELECTION);
    } else {
      // Completely new data context. All changes, sort order, filters and value changes
      // are no longer valid.
      updatePmTable();
    }
  }
  
  @Override
  protected boolean isPmVisibleImpl() {
    if(!super.isPmVisibleImpl()) {
      return false;
    }
    
    if(getOwnMetaData().getVisibilityCfg() == Visible.IF_NOT_EMPTY) {
      return getTotalNumOfPmRows() > 0;
    }
    
    return true;
  }

  @Override
  public List<PmTableCol> getColumnPms() {
    return PmUtil.getPmChildrenOfType(this, PmTableCol.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_ROW_PM> getRowPms() {
    return isPmVisible()
        ? getPmPageableCollection().getItemsOnPage()
        : Collections.EMPTY_LIST;
  }

  /** @deprecated Please use {@link #getRowPms()} */
  @Deprecated
  @Override
  public final List<T_ROW_PM> getRows() {
    return getRowPms();
  }

  @Override
  public long getTotalNumOfPmRows() {
    return getPmPageableCollection().getNumOfItems();
  }

  public SelectMode getPmRowSelectMode() {
    if (pmRowSelectMode == null) {
      pmRowSelectMode = getOwnMetaDataWithoutPmInitCall().rowSelectMode;
      if (pmRowSelectMode == SelectMode.DEFAULT) {
        // XXX oboede: add a configurable default?
        pmRowSelectMode = SelectMode.NO_SELECTION;
      }
    }

    return pmRowSelectMode;
  }

  /**
   * Adjusts the row selection mode.<br>
   * Should be called very early within the life cycle of the table.<br>
   * The implementation does currently not fire any change events
   * when this method gets called.
   *
   * @param rowSelectMode The {@link SelectMode} to be used by this table.
   */
  public void setPmRowSelectMode(SelectMode rowSelectMode) {
    // the default case is internally represented by having a 'null' value.
    this.pmRowSelectMode = (rowSelectMode != SelectMode.DEFAULT)
        ? rowSelectMode
        : null;

    if (pmPageableCollection != null) {
      pmPageableCollection.getSelectionHandler().setSelectMode(getPmRowSelectMode());
    }
  }

  @Override
  public int getNumOfPageRowPms() {
    if (numOfPageRowPms == null) {
      numOfPageRowPms = getOwnMetaDataWithoutPmInitCall().numOfPageRowPms;
    }
    return numOfPageRowPms;
  }

  /**
   * @param numOfPageRows
   *          The number of rows per page. <br>
   *          If it is <code>null</code> the statically defined number of rows
   *          will be used.
   */
  public void setNumOfPageRowPms(Integer numOfPageRows) {
    this.numOfPageRowPms = numOfPageRows;
    if (pmPageableCollection != null) {
      pmPageableCollection.setPageSize(numOfPageRows);
    }
  }

  /** @deprecated Please use <code>getPmPageableBeanCollection().getSelectionHandler()</code> or <code>getPmPageableCollection().getSelectionHandler()</code>. */
  @Override
  @Deprecated
  public SelectionHandler<T_ROW_PM> getPmSelectionHandler() {
    return getPmPageableCollection().getSelectionHandler();
  }

  @Override
  public final T_ROW_PM getMasterRowPm() {
    if (masterRowPm == null) {
      masterRowPm = getMasterRowPmImpl();
    }
    return masterRowPm;
  }

  /**
   * Provides the master row logic behind the (cached)
   * {@link #getMasterRowPm()} method.
   * <p>
   * The default implementation provides the selected item in case of single
   * selection mode. For other modes it provides <code>null</code>.
   *
   * @return the master row. <code>null</code> if there is no master row.
   */
  protected T_ROW_PM getMasterRowPmImpl() {
    if (getPmRowSelectMode() == SelectMode.SINGLE) {
      Selection<T_ROW_PM> selection = getPmSelectionHandler().getSelection();
      if (selection.getSize() == 1) {
        return selection.iterator().next();
      }
    }
    return null;
  }

  /**
   * Provides the bean behind the master row PM.<br>
   * See {@link #getMasterRowPm()}.
   *
   * @return the bean behind the currently active master row PM or <code>null</code>.
   */
  public T_ROW_BEAN getMasterRowPmBean() {
    T_ROW_PM rowPm = getMasterRowPm();
    return (rowPm != null)
        ? rowPm.getPmBean()
        : null;
  }

  /**
   * INTERNAL method that manually clears the cached master row PM.
   * <p>
   * Is helpful for implementations that don't use the selection of the pageable collection
   * to define the master row.
   */
  public void clearMasterRowPm() {
    if ((masterRowPm != null) && LOG.isTraceEnabled()) {
      LOG.trace(this + " - clearing master row.");
    }
    this.masterRowPm = null;
  }

  /**
   * Short cut method to get the {@link QueryParams} of the pageable collection.
   *
   * @return the query behind this table.
   */
  public final QueryParams getPmQueryParams() {
    return getPmPageableCollection().getQueryParams();
  }

  /**
   * Provides the filter compare definition factory used for this table.<br>
   * It defines how the filter definitions for user defined filter dialogs will
   * be created.
   * <p>
   * It may be adjusted by defining one in
   * {@link PmDefaults#getFilterCompareDefinitionFactory()} or by overriding
   * this method.
   *
   * @return the factory.
   */
  public FilterDefinitionFactory getPmFilterCompareDefinitionFactory() {
    return getPmConversation().getPmDefaults().getFilterCompareDefinitionFactory();
  }

  @Override
  public void addPmDecorator(PmCommandDecorator decorator, TableChange... changes) {
    if (pmChangeDecoratorMap.isEmpty()) {
      pmChangeDecoratorMap = new HashMap<PmTable.TableChange, PmCommandDecoratorSetImpl>();
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
  public final void updatePmTable(UpdateAspect... clearOptions) {
    UpdateAspect[] toProcess = clearOptions.length == 0
                      ? UpdateAspect.values()
                      : clearOptions;

    for (UpdateAspect o : toProcess) {
      clearPmAspectImpl(o);
    }
  }

  /**
   * Implements the table subclass specific update operation.
   * <p>
   * Should not be called directly. Please use {@link #updatePmTable(org.pm4j.core.pm.PmTable.ClearAspect...)}
   * to trigger an update.
   *
   * @param clearAspect the PM aspect to clear.
   */
  protected void clearPmAspectImpl(UpdateAspect clearAspect) {
    switch (clearAspect) {
    case CLEAR_SELECTION:
        // the 'current' row corrensponds in most case to the selection. It needs to be re-calculated.
      clearMasterRowPm();
      // In case of a clear call we do not handle vetos.
        // TODO olaf: Write unit tests to verify that that's not problem in all master details cases.
      SelectionHandlerUtil.forceSelectAll(getPmPageableCollection().getSelectionHandler(), false);

        // Ensure that the minimal standard selection gets re-created on next get-selection request.
        // TODO: can be part of selectAll(false). Every selection that leads to a no-Selection.
      getPmPageableCollection().getSelectionHandler().ensureSelectionStateRequired();
      break;
    case CLEAR_SORT_ORDER:
      getPmQueryParams().setSortOrder(getPmQueryOptions().getDefaultSortOrder());
      break;
    case CLEAR_CHANGES:
      PmCacheApi.clearPmCache(this);
      ModificationHandler<T_ROW_PM> mh = getPmPageableCollection().getModificationHandler();
      // a null check for very specific read-only collections
      if (mh != null && mh.getModifications().isModified()) {
        mh.clear();
      }
        // Ensure that the row PM's will be re-created. Otherwise it can happen that
      // a row with a stale object reference stays alive.
      BeanPmCacheUtil.clearBeanPmCachesOfSubtree(PmTableImpl.this);

      break;
    case CLEAR_USER_FILTER:
      clearMasterRowPm();
        // User filters can't be cleared on this level. More detailed implementations
      // may implement user defined filters that may be cleared.
      break;
      default: throw new PmRuntimeException(this, "Unknown clear aspect: " + clearAspect);
    }
  }

  @Override
  protected void clearCachedPmValues(Set<CacheKind> cacheSet) {
    super.clearCachedPmValues(cacheSet);
    if (cacheSet.contains(CacheKind.VALUE)) {
      getPmPageableCollection().clearCaches();
      clearMasterRowPm();
      getPmPageableCollection().getSelectionHandler().ensureSelectionStateRequired();
    }
  }

  /** Calls {@link #updatePmTable(org.pm4j.core.pm.PmTable.UpdateAspect...)}. */
  @Override
  public void resetPmValues() {
    updatePmTable();
    super.resetPmValues();
  }

  /** @deprecated please use {@link #getColumnPms()} */
  @Override
  public final List<PmTableCol> getColumns() {
    return getColumnPms();
  }

  /** @deprecated please use {@link #getTotalNumOfPmRows()} */
  @Override
  public final int getTotalNumOfRows() {
    return (int) getTotalNumOfPmRows();
  }

  /** @deprecated Please use {@link #getPmRowSelectMode()} */
  public final SelectMode getRowSelectMode() {
    return getPmRowSelectMode();
  }

  /** @deprecated Please use {@link #setPmRowSelectMode(SelectMode)} */
  public final void setRowSelectMode(SelectMode rowSelectMode) {
    setPmRowSelectMode(rowSelectMode);
  }

  /** @deprecated Please use {@link #getNumOfPageRowPms()} */
  @Override
  public final int getNumOfPageRows() {
    return getNumOfPageRowPms();
  }

  /** @deprecated Please use {@link #setNumOfPageRowPms(Integer)} */
  public final void setNumOfPageRows(Integer numOfPageRows) {
    setNumOfPageRowPms(numOfPageRows);
  }

  // -- helper methods --

  @Override
  protected boolean isPmValueChangedImpl() {
    return getPmPageableCollection().getModifications().isModified() || super.isPmValueChangedImpl();
  }

  @Override
  protected void setPmValueChangedImpl(boolean changed) {
    super.setPmValueChangedImpl(changed);
    if (changed == false) {
      for (T_ROW_PM p : getPmPageableCollection().getModifications().getUpdatedItems()) {
        p.setPmValueChanged(false);
      }
      getPmPageableCollection().getModificationHandler().clear();
    }
  }

  /**
   * {@link PmTable} validation logic.
   */
  public static class TableValidator extends ObjectValidator<PmTableImpl<PmBean<?>, ?>> {

    private Set<RowsToValidate> rowsToValidate = new HashSet<RowsToValidate>();
    private long itemNumWarningLimit = 5000;

    /**
     * @param rowValidationDefinitions Specifies the row set(s) to validate.
     */
    public TableValidator(RowsToValidate... rowValidationDefinitions) {
      rowsToValidate.addAll(Arrays.asList(rowValidationDefinitions));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Iterable<PmObject> getChildrenToValidate(PmTableImpl<PmBean<?>, ?> pm) {
      Iterable<PmObject> otherChildrenToValidate = super.getChildrenToValidate(pm);

      if (rowsToValidate.contains(RowsToValidate.ALL)) {
        checkRowNumLimit(pm, pm.getTotalNumOfPmRows());
        return IterableUtil.join(otherChildrenToValidate, pm.getPmPageableCollection());
      }

      // A linked set is used to get a validation error message order that somehow
      // corresponds to the row sort order.
      // The developer may define a different position for added records, but
      // we don't have the exact record positions here (would be to expensive to calculate).
      Set<PmObject> rows = new LinkedHashSet<PmObject>();
      rows.addAll(IterableUtil.asCollection(super.getChildrenToValidate(pm)));

      // messages for visible area first
      if (rowsToValidate.contains(RowsToValidate.CURRENT_PAGE)) {
        rows.addAll(pm.getRowPms());
      }
      if (rowsToValidate.contains(RowsToValidate.UPDATED)) {
        rows.addAll(pm.getPmPageableCollection().getModifications().getUpdatedItems());
      }
      // added items are in most cases displayed at the end of a table
      if (rowsToValidate.contains(RowsToValidate.ADDED)) {
        rows.addAll(pm.getPmPageableCollection().getModifications().getAddedItems());
      }

      checkRowNumLimit(pm, rows.size());
      return IterableUtil.join(otherChildrenToValidate, rows);
    }

    /**
     * If set to <code>false</code> only the added and modified row PMs will be validated.
     * This is the default setting.<br>
     * If set to <code>true</code> all row PMs will be validated.
     * <p>
     * WARNING: Validation of all rows may lead to bad performance in case of large tables.
     *
     * @param validateAllRows The switch.
     *
     * @deprecated Please use constructor parameter configuration.
     */
    @Deprecated
    public void setValidateAllRows(boolean validateAllRows) {
      if (validateAllRows) {
        this.rowsToValidate.add(RowsToValidate.ALL);
      } else {
        this.rowsToValidate.remove(RowsToValidate.ALL);
      }
    }

    /**
     * If more than the configured number of rows has to be validated, the method
     * {@link #checkRowNumLimit(PmTableImpl, long)} will log a warning.
     *
     * @param itemNumWarningLimit The max. rows to validate limit.
     */
    public void setItemNumWarningLimit(int itemNumWarningLimit) {
      this.itemNumWarningLimit = itemNumWarningLimit;
    }

    /**
     * Generates a log warning if the limit is exceeded.<br>
     * Subclasses may override this method to perform their specific action.
     *
     * @param pm The table to validate.
     * @param numOfItemsToValidate The validation item number limit.
     */
    protected void checkRowNumLimit(PmTableImpl<PmBean<?>, ?> pm, long numOfItemsToValidate) {
      if (numOfItemsToValidate > itemNumWarningLimit) {
        LOG.warn(pm.getPmRelativeName() + ": Performance warning - In memory validation for " + numOfItemsToValidate + " row PMs started.");
      }
    }
  }

  @Override
  protected Validator makePmValidator() {
    return getOwnMetaDataWithoutPmInitCall().deprValidation
        ? new DeprTableValidator()
        : new TableValidator(RowsToValidate.UPDATED, RowsToValidate.ADDED);
  }

  /**
   * @return The {@link PageableCollection} that handles the table row PM's to display.
   */
  @Override
  public final PageableCollection<T_ROW_PM> getPmPageableCollection() {
    zz_ensurePmInitialization();
    if (pmPageableCollection == null) {
      assignPmPageableCollection(getPmPageableCollectionImpl());
    }
    return pmPageableCollection;
  }

  /**
   * This method provides access to a collection that handles the beans behind
   * the row PMs.
   * <p>
   * @return The {@link PageableCollection} that handles the table row beans to display.
   */
  @SuppressWarnings("unchecked")
  public final PageableCollection<T_ROW_BEAN> getPmPageableBeanCollection() {
    return ((PmBeanCollection<T_ROW_PM, T_ROW_BEAN>) getPmPageableCollection()).getBeanCollection();
  }

  /**
   * Provides for service based tables the backing service reference.
   * <p>
   * The base implementation provides a reference to the service configured in {@link PmTableCfg#queryServiceClass()}.
   *
   * @return The used service in case of service based tables. <code>null</code> in case of in-memory tables.
   */
  @SuppressWarnings("unchecked")
  protected QueryService<T_ROW_BEAN, ?> getPmQueryServiceImpl() {
    MetaData md = getOwnMetaData();
    if (md.serviceClass != null) {
      // TODO oboede: add a type based service locator.
      String lookupName = StringUtils.uncapitalize(md.serviceClass.getSimpleName());
      QueryService<T_ROW_BEAN, ?> service = (QueryService<T_ROW_BEAN, ?>) getPmConversationImpl().getPmNamedObject(lookupName);

      if (service == null) {
        throw new PmRuntimeException(this, "No implementation found for the serviceClass configured in @PmTableCfg. Configured " + service);
      }
      return service;
    }
    // no service available. Is OK for collection bound tables.
    return null;
  }

  /**
   * Gets called whenever the internal {@link #pmPageableCollection} is <code>null</code> and
   * {@link #getPmPageableCollection()} gets called.
   *
   * @return The collection to use. Never <code>null</code>.
   */
  protected PmBeanCollection<T_ROW_PM, T_ROW_BEAN> getPmPageableCollectionImpl() {
    @SuppressWarnings("unchecked")
    QueryService<T_ROW_BEAN, Object> service = (QueryService<T_ROW_BEAN, Object>) getPmQueryServiceImpl();
    QueryOptions qo = getPmQueryOptions();
    PageableCollection<T_ROW_BEAN> pc = makePmPageableBeanCollection(service, qo);
    if (service != null) {
      pmCollectionGetterLogicUsed = true; // From now on setPmBeans() shouldn't be called.
    }
    return new PmBeanCollection<T_ROW_PM, T_ROW_BEAN>(this, PmBean.class, pc);
  }

  /**
   * Factory method that may be overridden to create a more specific collection.<br>
   * It will only be called if the table handles in-memory data (if
   * {@link #getPmQueryServiceImpl()} returns <code>null</code>).
   *
   * @return The in-memory collection to be used for this table.
   */
  protected PageableCollection<T_ROW_BEAN> makePmPageableBeanCollection(QueryService<T_ROW_BEAN, Object> service, QueryOptions qo) {
    if (service == null) {
      return makePmPageableInMemBeanCollection(null, qo);
    }
    if (service instanceof PageQueryService) {
      return new PageQueryCollection<T_ROW_BEAN, Object>((PageQueryService<T_ROW_BEAN, Object>) service, qo);
    }
    if (service instanceof IdQueryService) {
      return new IdQueryCollectionImpl<T_ROW_BEAN, Object>((IdQueryService<T_ROW_BEAN, Object>) service, qo);
    }
    // a different service type:
    throw new PmRuntimeException(this,
        "The service type is not a 'PageQueryService' and not a 'IdQueryService'. Possibly @PmTableCfg#serviceClass is not well configured.\n" +
        "\tFound service: " + service +
        "\tAlternatively you may override makePmPageableBeanCollection() to support create a your specific collection.");
  }

  /**
   * Factory method that may be overridden to create a more specific in memory
   * collection.<br>
   * It will only be called if the table handles in-memory data.
   *
   * @return The in-memory collection to be used for this table.
   */
  protected InMemCollection<T_ROW_BEAN> makePmPageableInMemBeanCollection(Collection<T_ROW_BEAN> beans, QueryOptions qo) {
    if (beans == null) {
      return new InMemTableBeanCollection(qo);
    } else {
      return new InMemCollectionImpl<T_ROW_BEAN>(beans, qo);
    }
  }

  /**
   * Reads the {@link QueryOptions} to using the information provided by the given {@link org.pm4j.core.pm.PmTable}
   * and {@link QueryService}.
   *
   * @return The evaluated {@link QueryOptions} instance. Never <code>null</code>.
   */
  protected QueryOptions getPmQueryOptions() {
    QueryOptionProvider qop = getPmQueryOptionProvider();
    return (qop != null)
        ? qop.getQueryOptions()
        : new QueryOptions();
  }

  /**
   * If the optionally existing backing query service is an {@link QueryOptionProvider}, this service
   * will be returned.
   * <p>
   * If the there is no backing service an {@link InMemQueryOptionProvider} will be returned.
   *
   * @return The option provider or <code>null</code>.
   */
  protected QueryOptionProvider getPmQueryOptionProvider() {
    QueryService<T_ROW_BEAN, ?> service = getPmQueryServiceImpl();
    if (service != null) {
      return (service instanceof QueryOptionProvider)
          ? (QueryOptionProvider)service
          : null;
    } else {
      return new InMemQueryOptionProvider();
    }
  }

  /**
   * Used for in-memory tables.<br>
   * The default implementation of {@link #getPmPageableCollectionImpl()} asks this method for the
   * set of beans to represent.
   * <p>
   * The default implementation uses either the configured {@link PmTableCfg#valuePath()}.<br>
   * If that is not configured the expression <code>"(o)pmBean.<pmName of my table>"</code>
   * will be used. This default expression is often useful for tables in {@link PmBean}s that represent a
   * bean collection having the same name.
   *
   * @return the set of beans to display. May be <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  protected Collection<T_ROW_BEAN> getPmBeansImpl() {
    PathResolver valuePathResolver = getOwnMetaData().valuePathResolver;
    Collection<T_ROW_BEAN> beans = (valuePathResolver != null)
      ? (Collection<T_ROW_BEAN>) valuePathResolver.getValue(getPmParent())
      : Collections.EMPTY_LIST;
    return beans;
  }

  @SuppressWarnings("unchecked")
  protected Class<T_ROW_PM> getPmRowBeanClass() {
    Type t = GenericTypeUtil.resolveGenericArgument(PmTableImpl.class, getClass(), 1);
    if (t == null) {
      throw new PmRuntimeException(this, "Unable to determine table row bean class. Please check your generics parametes or override getPmRowBeanClass().");
    }

    try {
      return (Class<T_ROW_PM>) t;
    } catch (ClassCastException e) {
      throw new PmRuntimeException(this, "Unable to determine table row bean class. Please check your generics parametes or override getPmRowBeanClass().", e);
    }
  }

  /**
   * Defines the data set to be presented by the table.
   * 
   * <p>ATTENTION: Some unit test setups might be easier using this method, but it 's strongly 
   * discouraged to use it in production code; some features might not work properly, e.g. filtering.
   * For production code, overriding {@link #getPmBeansImpl()} is recommended.</p>
   *
   * @param pageable
   *          the data set to present. If it is <code>null</code> an empty
   *          collection will be created internally by the next {@link #getPmPageableCollection()} call.
   */
  @SuppressWarnings("deprecation")
  public void setPmPageableCollection(PmBeanCollection<T_ROW_PM, T_ROW_BEAN> pageable) {
    Selection<T_ROW_PM> selection = null;

    if (pmPageableCollection != null) {
      selection = pmPageableCollection.getSelectionHandler().getSelection();
    }

    assignPmPageableCollection(pageable);
    PmEventApi.firePmEventIfInitialized(this, PmEvent.VALUE_CHANGE, ValueChangeKind.VALUE);

    // re-apply the settings to preserve
    if (selection != null) {
      // ensure that the internal field is set, even it was just reset to null.
      getPmPageableCollection();
      // XXX oboede: Move that method to a utility that is not visible for the programmer.
      pmPageableCollection.getSelectionHandler().setSelection(selection);
    }
  }

  /**
   * @return A pager that may be used to navigate through the table.<br>
   *         May return <code>null</code> if there is no pager defined for this
   *         table.
   */
  public PmPager getPmPager() {
    return null;
  }

  /**
   *
   * A post processing method that allow to apply some default settings to a new pageable collection.
   * <p>
   * Gets called whenever a new {@link #pmPageableCollection} gets assigned:
   * <ul>
   * <li>by calling {@link #getPmPageableCollectionImpl()} or</li>
   * <li>by calling {@link #setPmPageableCollection(PmBeanCollection)}</li>
   * </ul>
   * The default settings applied in this base implementation are:
   * <ul>
   * <li>Number of page rows and multi-select setting.</li>
   * <li>The reference of the (optional) pager to the collection.</li>
   * </ul>
   * Sub classes may override this method to extend this logic.
   *
   * @param pc the collection to initialize.
   */
  protected void assignPmPageableCollection(final PmBeanCollection<T_ROW_PM, T_ROW_BEAN> pc) {
    this.pmPageableCollection = pc;
    if (pageableCollectionEventAdapter != null) {
      pageableCollectionEventAdapter.unregisterListeners();
      pageableCollectionEventAdapter = null;
    }

    if (pmPageableCollection != null) {
      pmPageableCollection.getSelectionHandler().setSelectMode(getPmRowSelectMode());
      pmPageableCollection.setPageSize(getNumOfPageRowPms());

      pageableCollectionEventAdapter = new InternalPmTableEventAdapterForPageableCollection(this, pmPageableCollection);
      pageableCollectionEventAdapter.registerListeners();
    }

    // XXX olaf: Check - is redundant to the change listener within Pager!
    if (getPmPager() != null) {
      getPmPager().setPageableCollection(pmPageableCollection);
    }
  }

  @Override
  public PmTable.ImplDetails getPmImplDetails() {
    return new TableDetailsImpl();
  }

  // -- support classes --

  /**
   * An in-memory collection that uses the table specific context:
   * <ul>
   * <li>uses the table value cache strategy, configured in {@link PmCacheCfg#value()}, to control the frequency of <code>getPmBeansImpl()</code> calls.</li>
   * <li>binds the backing collection to {@link PmTableImpl#getPmBeansImpl()}.</li>
   * </ul>
   */
  protected class InMemTableBeanCollection extends InMemCollectionBase<T_ROW_BEAN> {

    /**
     * @param queryOptions
     */
    public InMemTableBeanCollection(QueryOptions queryOptions) {
      super(queryOptions);
      setCacheStrategy(getOwnMetaData().inMemCollectionCacheStragegy, PmTableImpl.this);
    }

    @Override
    protected Collection<T_ROW_BEAN> getBackingCollectionImpl() {
      Collection<T_ROW_BEAN> beans = getPmBeansImpl();
      if (beans != null && !beans.isEmpty()) {
        // After providing some beans using the getter logic setPmBeans() shouldn't be used.
        // This could cause an inconsistent table model behavior.
        pmCollectionGetterLogicUsed = true;
      }
      return beans;
    }
  }

  /**
   * Uses the table annotations to generate the {@link QueryOptions} for this table.
   */
  public class InMemQueryOptionProvider implements QueryOptionProvider {
    @Override
    public QueryOptions getQueryOptions() {
      QueryOptions options = new QueryOptions();

      // * Read the table definitions
      PmTableImpl<?, ?> pmTable = PmTableImpl.this;
      MetaData md = PmTableImpl.this.getOwnMetaDataWithoutPmInitCall();

      // * Read the column definitions
      FilterDefinitionFactory ff = pmTable.getPmFilterCompareDefinitionFactory();
      boolean tableSortable = pmTable.getPmImplDetails().isSortable();
      for (PmTableCol col : pmTable.getColumnPms()) {
        PmTableCol.ImplDetails d = col.getPmImplDetails();
        if ((d.isSortableConfigured() == Boolean.TRUE) ||
            (d.isSortableConfigured() == null && tableSortable)) {
          options.addSortOrder(d.getQueryAttr());
        }

        FilterDefinition fcd = d.getFilterCompareDefinition(ff);
        if (fcd != null) {
          options.addFilterCompareDefinition(fcd);
        }
      }

      // * The 'initialSortCol' can only be evaluated after processing the column sort options.
      if (md.initialSortColName != null) {
        String name = StringUtils.substringBefore(md.initialSortColName, ",");
        SortOrder so = options.getSortOrder(name);
        if (so == null) {
          throw new PmRuntimeException(pmTable, "initial sort column '" + md.initialSortColName + "' is not a sortable column.");
        }
        if ("desc".equals(StringUtils.trim(StringUtils.substringAfter(md.initialSortColName, ",")))) {
          so = so.getReverseSortOrder();
        }
        options.setDefaultSortOrder(so);
      }

      Comparator<?> initialSortComparator = getInitialSortOrderComparator();
      if (initialSortComparator != null) {
        if (options.getDefaultSortOrder() != null) {
          throw new PmRuntimeException(pmTable, "initialSortCol and initialBeanSortComparator found in PmTableCfg annotation. Don't know what to sort by.");
        }
        options.setDefaultSortOrder(new InMemSortOrder(initialSortComparator));
      }

      return options;
    }

    /**
     * Provides the configured initial sort order comparator.
     * <p>
     * May be overridden to provide a domain specific comparator.
     * @return
     */
    protected Comparator<?> getInitialSortOrderComparator() {
      MetaData md = PmTableImpl.this.getOwnMetaDataWithoutPmInitCall();
      return (md.initialBeanSortComparatorClass != null)
          ? (Comparator<?>)ClassUtil.newInstance(md.initialBeanSortComparatorClass)
          : null;
    }
  }

  /** Caches the backing collection locally in {@link PmTableImpl#pmInMemCollectionCache}. */
  protected static class CacheStrategyImMemCollectionReference extends CacheStrategyBase<PmTableImpl<?, ?>> {

    /** @param cacheName A cache name for reporting only. */
    public CacheStrategyImMemCollectionReference(String cacheName) {
      super(cacheName);
    }

    @Override
    protected Object readRawValue(PmTableImpl<?, ?> pm) {
      return pm.pmInMemCollectionCache;
    }

    @Override
    protected void writeRawValue(PmTableImpl<?, ?> pm, Object value) {
      pm.pmInMemCollectionCache = value;
    }

    @Override
    protected void clearImpl(PmTableImpl<?, ?> pm) {
      pm.pmInMemCollectionCache = null;
    }
  };

  /** Implements controlled implementation layer access for other PM classes. */
  protected class TableDetailsImpl implements ImplDetails {
    @Override
    public boolean isSortable() {
      return getOwnMetaDataWithoutPmInitCall().sortable;
    }
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
          setPageableCollection(PmTableImpl.this.getPmPageableCollection());
        }
      });
    }
  }

  // ======== meta data ======== //

  @Override
  protected MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    @SuppressWarnings("unchecked")
    MetaData myMetaData = (MetaData) metaData;

    PmTableCfg cfg = AnnotationUtil.findAnnotation(this, PmTableCfg.class);
    if (cfg != null) {
      myMetaData.rowSelectMode = cfg.rowSelectMode();
      if (cfg.numOfPageRows() > 0) {
        myMetaData.numOfPageRowPms = cfg.numOfPageRows();
      }

      myMetaData.sortable = cfg.sortable();
      myMetaData.initialBeanSortComparatorClass = (cfg.initialSortComparator() != Comparator.class)
                                ? cfg.initialSortComparator()
                                : null;
      myMetaData.initialSortColName = StringUtils.defaultIfEmpty(cfg.initialSortCol(), null);
      myMetaData.serviceClass = (cfg.queryServiceClass() != QueryService.class)
                                ? cfg.queryServiceClass()
                                : null;
      if (myMetaData.serviceClass != null) {
        if (StringUtils.isNotBlank(cfg.valuePath())) {
          throw new PmRuntimeException(this, "PmTableCfg.serviceClass and -.valuePath are specified. From which of these sources the table data be read from?");
        }
        if (myMetaData.initialBeanSortComparatorClass != null) {
          throw new PmRuntimeException(this, "PmTableCfg.serviceClass and -.initialBeanSortComparator are specified. A service (usually database based) can't use a comparator for sort operations.");
        }
      }
    }

    // -- initialize the optional path resolver for in-memory tables. --
    if (myMetaData.serviceClass == null) {
      // Initialize the optional path resolver. --
      String valuePath = ((cfg != null) && StringUtils.isNotEmpty(cfg.valuePath()))
          ? valuePath = cfg.valuePath()
          : (getPmParent() instanceof PmBean) && StringUtils.isNotBlank(getPmName())
              ? "(o)pmBean." + getPmName()
              : "";
      if (StringUtils.isNotBlank(valuePath)) {
        myMetaData.valuePathResolver = PmExpressionPathResolver.parse(valuePath, PmExpressionApi.getSyntaxVersion(this));
      }
      // TODO oboede: Missing support for new cache annotation.
      myMetaData.inMemCollectionCacheStragegy = DeprAnnotationUtil.readCacheStrategy(this, PmCacheCfg.ATTR_VALUE, CACHE_STRATEGIES_FOR_IN_MEM_COLLECTION);
    }
  }

  protected class MetaData extends PmObjectBase.MetaData {
    private SelectMode rowSelectMode = SelectMode.DEFAULT;
    private int numOfPageRowPms = DEFAULT_NUM_OF_PAGE_ROW_PMS;
    private PathResolver valuePathResolver;
    @SuppressWarnings("rawtypes")
    private Class<? extends QueryService> serviceClass;
    private boolean sortable;
    private Class<?> initialBeanSortComparatorClass = null;
    private String initialSortColName = null;
    private CacheStrategy inMemCollectionCacheStragegy = CacheStrategyNoCache.INSTANCE;

    /** May be used to define a different default value. */
    public void setNumOfPageRowPms(int numOfPageRowPms) { this.numOfPageRowPms = numOfPageRowPms; }
  }

  @SuppressWarnings("unchecked")
  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

  @SuppressWarnings("unchecked")
  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

}
