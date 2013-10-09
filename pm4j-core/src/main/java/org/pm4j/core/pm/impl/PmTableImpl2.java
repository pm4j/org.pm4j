package org.pm4j.core.pm.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.pageable.ItemIdService;
import org.pm4j.common.pageable.ModificationHandler;
import org.pm4j.common.pageable.Modifications;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.inmem.PageableInMemCollectionBase;
import org.pm4j.common.pageable.querybased.PageableQueryCollection;
import org.pm4j.common.pageable.querybased.PageableQueryService;
import org.pm4j.common.pageable.querybased.QueryOptionProvider;
import org.pm4j.common.pageable.querybased.idquery.PageableIdQueryCollectionImpl;
import org.pm4j.common.pageable.querybased.idquery.PageableIdQueryService;
import org.pm4j.common.query.FilterCompareDefinition;
import org.pm4j.common.query.FilterCompareDefinitionFactory;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmPager2;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol2;
import org.pm4j.core.pm.PmTableGenericRow2;
import org.pm4j.core.pm.annotation.PmTableCfg2;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;
import org.pm4j.core.pm.pageable2.PageablePmBeanCollection;
import org.pm4j.core.util.reflection.ClassUtil;
import org.pm4j.core.util.reflection.GenericTypeUtil;

/**
 * A table that presents the content of a set of {@link PmElement}s.
 * <p>
 * The table data related logic is provided by a {@link PageableCollection2}.
 * This collection supports the logic for
 * <ul>
 * <li>pagination (see {@link PageableCollection2#setPageIdx(long)} etc.).</li>
 * <li>row selection</li>
 * <li>sorting</li>
 * <li>filtering (see TODO: ...)</li>
 * </ul>.
 * <p>
 *
 * @author olaf boede
 */
public class PmTableImpl2
        <T_ROW_PM extends PmBean<T_ROW_BEAN>, T_ROW_BEAN>
        extends PmDataInputBase
        implements PmTable2<T_ROW_PM> {

  /**
   * The default number of rows per page. Is used if no number of rows is specified
   * in {@link PmTableCfg2#numOfPageRows()} or by calling {@link #setNumOfPageRowPms(Integer)}.
   */
  public static final int DEFAULT_NUM_OF_PAGE_ROW_PMS = 10;

  private static final Log LOG = LogFactory.getLog(PmTableImpl2.class);

  /** The content this table is based on. */
  private PageablePmBeanCollection<T_ROW_PM, T_ROW_BEAN> pmPageableCollection;

  /** Defines the row-selection behavior. */
  private SelectMode pmRowSelectMode;

  /**
   * The number of rows per page. If it is <code>null</code> the statically defined number of rows will be used.
   */
  private Integer numOfPageRowPms;

  /** The set of decorators for various table change kinds. */
  private Map<TableChange, PmCommandDecoratorSetImpl> pmChangeDecoratorMap = Collections.emptyMap();

  /** Listens for selection changes and handles the table relates logic. */
  private TableSelectionChangeListener pmTableSelectionChangeListener = new TableSelectionChangeListener();

  /** Listens for filter changes and handles the table relates logic. */
  private TableFilterChangeListener pmTableFilterChangeListener = new TableFilterChangeListener();

  /** A cached reference to the selected master row. */
  private T_ROW_PM masterRowPm;

  /**
   * Creates a table PM.
   *
   * @param pmParent The presentation model context for this table.
   */
  public PmTableImpl2(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  public List<PmTableCol2> getColumnPms() {
    return PmUtil.getPmChildrenOfType(this, PmTableCol2.class);
  }

  @Override
  public List<PmTableGenericRow2<T_ROW_PM>> getGenericRowPms() {
    List<PmTableGenericRow2<T_ROW_PM>> genericRows = null;

    if (genericRows == null) {
      List<T_ROW_PM> rows = getRowPms();
      genericRows = new ArrayList<PmTableGenericRow2<T_ROW_PM>>(rows.size());
      for (T_ROW_PM r : rows) {
        genericRows.add(new PmTableGenericRowImpl2<T_ROW_PM>(this, r));
      }
    }

    return genericRows;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_ROW_PM> getRowPms() {
    return isPmVisible()
        ? getPmPageableCollection().getItemsOnPage()
        : Collections.EMPTY_LIST;
  }

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
   * Should be called very early within the livecycle of the table.
   * The implementation does currently not fire any change events
   * sif this method gets called.
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

  @Override
  protected boolean isPmReadonlyImpl() {
    return super.isPmReadonlyImpl() || getPmParent().isPmReadonly();
  }

  @Override
  public SelectionHandler<T_ROW_PM> getPmSelectionHandler() {
    return getPmPageableCollection().getSelectionHandler();
  }

  /**
   * @deprecated please use {@link #getMasterRowPm()}
   */
  public final T_ROW_PM getCurrentRowPm() {
    return getMasterRowPm();
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
   * @deprecated please use {@link #getMasterRowPmBean()}
   */
  public final T_ROW_BEAN getCurrentRowPmBean() {
    return getMasterRowPmBean();
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
  public QueryParams getPmQueryParams() {
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
  public FilterCompareDefinitionFactory getPmFilterCompareDefinitionFactory() {
    return getPmConversation().getPmDefaults().getFilterCompareDefinitionFactory();
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
   * Should not be called directly. Please use {@link #updatePmTable(org.pm4j.core.pm.PmTable2.ClearAspect...)}
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
        SelectionHandlerUtil.forceSelectAll(getPmSelectionHandler(), false);

        // Ensure that the minimal standard selection gets re-created on next get-selection request.
        // TODO: can be part of selectAll(false). Every selection that leads to a no-Selection.
        getPmSelectionHandler().ensureSelectionStateRequired();
        break;
      case CLEAR_SORT_ORDER:
        getPmQueryParams().setSortOrder(getPmPageableBeanCollection().getQueryOptions().getDefaultSortOrder());
        break;
      case CLEAR_CHANGES:
        PmCacheApi.clearPmCache(this);
        ModificationHandler<T_ROW_PM> mh = getPmPageableCollection().getModificationHandler();
        // a null check for very specific read-only collections
        if (mh != null) {
            mh.clearRegisteredModifications();
        }
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
      getPmSelectionHandler().ensureSelectionStateRequired();
    }
  }

  /** Calls {@link #updatePmTable(org.pm4j.core.pm.PmTable2.UpdateAspect...)}. */
  @Override
  public void resetPmValues() {
    updatePmTable();
    super.resetPmValues();
  }

  /** @deprecated please use {@link #getColumnPms()} */
  @Override
  public final List<PmTableCol2> getColumns() {
    return getColumnPms();
  }

  /** @deprecated please use {@link #getTotalNumOfPmRows()} */
  @Override
  public final int getTotalNumOfRows() {
    return (int)getTotalNumOfPmRows();
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
      getPmPageableCollection().getModificationHandler().clearRegisteredModifications();
    }
  }

  /**
   * Validates the changed row items only.
   */
  @Override
  public void pmValidate() {
    Modifications<T_ROW_PM> m = getPmPageableCollection().getModifications();
    List<T_ROW_PM> changes = ListUtil.collectionsToList(m.getAddedItems(), m.getUpdatedItems());
    for (T_ROW_PM itemPm : changes) {
      PmValidationApi.validateSubTree(itemPm);
    }
  }

  /**
   * @return The {@link PageableCollection2} that handles the table row PM's to display.
   */
  @Override
  public final PageableCollection2<T_ROW_PM> getPmPageableCollection() {
    zz_ensurePmInitialization();
    if (pmPageableCollection == null) {
      _setPageableCollection(getPmPageableCollectionImpl());
    }
    return pmPageableCollection;
  }

  /**
   * This method provides access to a collection that handles the beans behind
   * the row PMs.
   * <p>
   * @return The {@link PageableCollection2} that handles the table row beans to display.
   */
  @SuppressWarnings("unchecked")
  public PageableCollection2<T_ROW_BEAN> getPmPageableBeanCollection() {
    return ((PageablePmBeanCollection<T_ROW_PM, T_ROW_BEAN>)getPmPageableCollection()).getBeanCollection();
  }

  /**
   * Provides for service based tables the backing service reference.
   * <p>
   * The base implementation provides a reference to the service configured in @PmTable
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  protected ItemIdService<T_ROW_BEAN, ?> getPmQueryServiceImpl() {
    MetaData md = getOwnMetaData();
    if (md.serviceClass != null) {
      // TODO oboede: add a type based service locator.
      String lookupName = StringUtils.uncapitalize(md.serviceClass.getSimpleName());
      ItemIdService<T_ROW_BEAN, ?> service = (ItemIdService<T_ROW_BEAN, ?>) getPmConversationImpl().getPmNamedObject(lookupName);

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
  protected PageablePmBeanCollection<T_ROW_PM, T_ROW_BEAN> getPmPageableCollectionImpl() {
    @SuppressWarnings("unchecked")
    ItemIdService<T_ROW_BEAN, Object> s = (ItemIdService<T_ROW_BEAN, Object>) getPmQueryServiceImpl();
    QueryOptions qo = getPmQueryOptions();
    PageableCollection2<T_ROW_BEAN> pc = null;

    if (s == null) {
      pc = new PageableInMemCollectionBase<T_ROW_BEAN>(qo) {
        @Override
        protected Collection<T_ROW_BEAN> getBackingCollectionImpl() {
          return getPmBeansImpl();
        }
      };
    }
    else if (s instanceof PageableQueryService) {
      pc = new PageableQueryCollection<T_ROW_BEAN, Object>((PageableQueryService<T_ROW_BEAN, Object>) s, qo);
    }
    else if (s instanceof PageableIdQueryService) {
      pc = new PageableIdQueryCollectionImpl<T_ROW_BEAN, Object>((PageableIdQueryService<T_ROW_BEAN, Object>) s, qo);
    }
    else {
      throw new PmRuntimeException(this,
          "The service type provided by 'getPmQueryServiceImpl()' is not a 'PageableQueryService' and not a 'PageableIdQueryService'. Possibly @PmTableCfg#serviceClass is not well configured. Found serivce: " + s);
    }
    return new PageablePmBeanCollection<T_ROW_PM, T_ROW_BEAN>(this, PmBean.class, pc);
  }

  /**
   * Reads the {@link QueryOptions} to using the information provided by the given {@link TablePm2}
   * and {@link ItemIdService}.
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
    ItemIdService<T_ROW_BEAN, ?> service = getPmQueryServiceImpl();
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
   * The default implementation uses either the configured {@link PmTableCfg2#valuePath()}.<br>
   * If that is not configured the expression <code>"(o)pmBean.<pmName of my table>"</code>
   * will be used. This default expression is often useful for tables in {@link PmBean}s that represent a
   * bean collection having the same name.
   *
   * @return the set of beans to display. May be <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  protected Collection<T_ROW_BEAN> getPmBeansImpl() {
    PathResolver valuePathResolver = getOwnMetaData().valuePathResolver;
    if (valuePathResolver != null) {
      Collection<T_ROW_BEAN> beans = (Collection<T_ROW_BEAN>) valuePathResolver.getValue(getPmParent());
      return beans;
    } else {
      LOG.warn("The table PM '" + PmUtil.getPmLogString(this) + "' is not bound to any data to represent." +
          "\nYou may provide table data using the following ways:" +
          "\n\ta) bind to a Collection by defining a valuePath expresssion within a @PmTableCfg annotation"+
          "\n\tb) bind to a Collection by overriding getPmBeansImpl()"+
          "\n\tc) override getPmPageableCollectionImpl() to provide your specific pageable data source.");
      return Collections.EMPTY_LIST;
    }
  }

  /**
   * A post processing method that allow to apply some default settings to a new pageable collection.
   * <p>
   * Gets called whenever a new {@link #pmPageableCollection} gets assigned:
   * <ul>
   *  <li>by calling {@link #getPmPageableCollectionImpl()}</li>
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
    SelectionHandler<T_ROW_PM> selectionHandler = pageableCollection.getSelectionHandler();
    selectionHandler.setSelectMode(getPmRowSelectMode());
    pageableCollection.setPageSize(getNumOfPageRowPms());

    // XXX olaf: Check - is redundant to the change listener within Pager!
    if (getPmPager() != null) {
      getPmPager().setPageableCollection(pageableCollection);
    }
  }

  @SuppressWarnings("unchecked")
  protected Class<T_ROW_PM> getPmRowBeanClass() {
    Type t = GenericTypeUtil.resolveGenericArgument(PmTableImpl2.class, getClass(), 1);
    if (t == null) {
      throw new PmRuntimeException(this, "Unable to determine table row bean class. Please check your generics parametes or override getPmRowBeanClass().");
    }

    try {
      return (Class<T_ROW_PM>) t;
    } catch(ClassCastException e) {
      throw new PmRuntimeException(this, "Unable to determine table row bean class. Please check your generics parametes or override getPmRowBeanClass().", e);
    }
  }

  /**
   * Defines the data set to be presented by the table.<br>
   * HINT: Please check if you can define {@link #getPmPageableCollectionImpl()} instead.
   *
   * @param pageable
   *          the data set to present. If it is <code>null</code> an empty
   *          collection will be created internally by the next {@link #getPmPageableCollection()} call.
   */
  public void setPmPageableCollection(PageablePmBeanCollection<T_ROW_PM, T_ROW_BEAN> pageable) {
    Selection<T_ROW_PM> selection = null;

    if (pmPageableCollection != null) {
      selection = pmPageableCollection.getSelectionHandler().getSelection();
    }

    _setPageableCollection(pageable);
    PmEventApi.firePmEventIfInitialized(this, PmEvent.VALUE_CHANGE, ValueChangeKind.VALUE);

    // re-apply the settings to preserve
    if (selection != null) {
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
  private void _setPageableCollection(PageablePmBeanCollection<T_ROW_PM, T_ROW_BEAN> pc) {
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

  @Override
  public PmTable2.ImplDetails getPmImplDetails() {
    return new TableDetailsImpl();
  }

  // -- support classes --

  /**
   * Uses the table annotations to generate the {@link QueryOptions} for this table.
   */
  public class InMemQueryOptionProvider implements QueryOptionProvider {
    @Override
    public QueryOptions getQueryOptions() {
      QueryOptions options = new QueryOptions();

      // * Read the table definitions
      PmTableImpl2<?, ?> pmTable= PmTableImpl2.this;
      MetaData md = PmTableImpl2.this.getOwnMetaDataWithoutPmInitCall();

      // * Read the column definitions
      FilterCompareDefinitionFactory ff = pmTable.getPmFilterCompareDefinitionFactory();
      boolean tableSortable = pmTable.getPmImplDetails().isSortable();
      for (PmTableCol2 col : pmTable.getColumnPms()) {
        PmTableCol2.ImplDetails d = col.getPmImplDetails();
        if ((d.isSortableConfigured() == Boolean.TRUE) ||
            (d.isSortableConfigured() == null && tableSortable)) {
          SortOrder so = makeColSortOrder(col);
          options.addSortOrder(d.getQueryAttrName(), so);
        }

        FilterCompareDefinition fcd = d.getFilterCompareDefinition(ff);
        if (fcd != null) {
          options.addFilterCompareDefinition(fcd);
        }
      }

      // * The 'defaultSortCol' can only be evaluated after processing the column sort options.
      if (md.defaultSortColName != null) {
        String name = StringUtils.substringBefore(md.defaultSortColName, ",");
        SortOrder so = options.getSortOrder(name);
        if (so == null) {
          throw new PmRuntimeException(pmTable, "default sort column '" + md.defaultSortColName + "' is not a sortable column.");
        }
        if ("desc".equals(StringUtils.trim(StringUtils.substringAfter(md.defaultSortColName, ",")))) {
          so = so.getReverseSortOrder();
        }
        options.setDefaultSortOrder(so);
      }

      Comparator<?> initialSortComparator = getInitialSortOrderComparator();
      if (initialSortComparator != null) {
        if (options.getDefaultSortOrder() != null) {
          throw new PmRuntimeException(pmTable, "defaultSortCol and initialBeanSortComparator found in PmTableCfg annotation. Don't know what to sort by.");
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
      MetaData md = PmTableImpl2.this.getOwnMetaDataWithoutPmInitCall();
      return (md.initialBeanSortComparatorClass != Comparator.class)
          ? (Comparator<?>)ClassUtil.newInstance(md.initialBeanSortComparatorClass)
          : null;
    }

    protected QueryAttr getColQueryAttr(PmTableCol2 col) {
      PmTableCol2.ImplDetails d = col.getPmImplDetails();
      String name = d.getQueryAttrName();
      return new QueryAttr(name, name, Void.class, col.getPmTitle());
    }

    protected SortOrder makeColSortOrder(PmTableCol2 col) {
      return new SortOrder(getColQueryAttr(col));
    }
  }

  protected class TableDetailsImpl implements ImplDetails {
    @Override
    public boolean isSortable() {
      return getOwnMetaDataWithoutPmInitCall().sortable;
    }

  }

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
      // a command used for value change reporting.
      PmCommand cmd = new PmAspectChangeCommandImpl(PmTableImpl2.this, "selection", evt.getOldValue(), evt.getNewValue());
      for (PmCommandDecorator d : getPmDecorators(TableChange.SELECTION)) {
        if (!d.beforeDo(cmd)) {
          String msg = "Decorator prevents selection change: " + d;
          LOG.debug(msg);
          throw new PropertyVetoException(msg, evt);
        }
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      masterRowPm = null;
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
      masterRowPm = null;
      // FIXME: may fire too often a DB query. What happens in case of a series of QueryParam changes?
//      PageableCollectionUtil2.ensureCurrentPageInRange(getPmPageableCollection());

      for (PmCommandDecorator d : getPmDecorators(TableChange.FILTER)) {
        d.afterDo(null);
      }
    }
  }

  // ======== meta data ======== //

  @Override
  protected MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmDataInputBase.MetaData metaData) {
    super.initMetaData(metaData);
    @SuppressWarnings("unchecked")
    MetaData myMetaData = (MetaData) metaData;

    PmTableCfg2 cfg = AnnotationUtil.findAnnotation(this, PmTableCfg2.class);

    if (cfg != null) {
      myMetaData.rowSelectMode = cfg.rowSelectMode();
      if (cfg.numOfPageRows() > 0) {
        myMetaData.numOfPageRowPms = cfg.numOfPageRows();
      }

      myMetaData.sortable = cfg.sortable();
      myMetaData.initialBeanSortComparatorClass = cfg.initialBeanSortComparator();
      myMetaData.defaultSortColName = cfg.defaultSortCol();
      myMetaData.serviceClass = (cfg.serviceClass() != ItemIdService.class)
                                ? cfg.serviceClass()
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
      String valuePath = ((cfg != null) && StringUtils.isNotEmpty(cfg.valuePath()))
          ? valuePath = cfg.valuePath()
          : (getPmParent() instanceof PmBean) && StringUtils.isNotBlank(getPmName())
              ? "(o)pmBean." + getPmName()
              : "";
      if (StringUtils.isNotBlank(valuePath)) {
        myMetaData.valuePathResolver = PmExpressionPathResolver.parse(valuePath, PmExpressionApi.getSyntaxVersion(this));
      }
    }
  }

  protected class MetaData extends PmDataInputBase.MetaData {
    private SelectMode rowSelectMode = SelectMode.DEFAULT;
    private int numOfPageRowPms = DEFAULT_NUM_OF_PAGE_ROW_PMS;
    private PathResolver valuePathResolver;
    @SuppressWarnings("rawtypes")
    private Class<? extends ItemIdService> serviceClass;
    private boolean sortable;
    private Class<?> initialBeanSortComparatorClass = Comparator.class;
    private String defaultSortColName = null;


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
