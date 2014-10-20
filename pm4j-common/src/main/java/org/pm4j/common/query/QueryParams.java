package org.pm4j.common.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.pageable.querybased.pagequery.PageQueryService;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;

/**
 * Interface for query specifications.
 * <p>
 * It support the definition of {@link QueryExpr}s, {@link SortOrder} and
 * additional application specific base query parameters.
 * <p>
 * Technology specific query evaluators read these definitions for their specific
 * query evaluation.
 * <p>
 * See {@link InMemQueryEvaluator} and {@link PageQueryService} for usage examples.
 *
 * @author Olaf Boede
 */
public class QueryParams extends PropertyChangeSupportedBase implements Cloneable, Serializable {

  /** Identifier of the property change event that gets fired if the effective sort order gets changed. */
  public static final String PROP_EFFECTIVE_SORT_ORDER = "effectiveSortOrder";

  /** Identifier of the property change event that gets fired if the effective query filter gets changed. */
  public static final String PROP_EFFECTIVE_FILTER = "effectiveFilter";

  private static final long serialVersionUID = 1L;

  private SortOrder sortOrder;
  private SortOrder defaultSortOrder;
  private QueryExpr filterExpression;
  private Map<String, Object> baseQueryParams = new HashMap<String, Object>();
  /** A switch to allow/prevent query execution. */
  private boolean execQuery = true;

  /**
   * Creates a new empty instance.
   */
  public QueryParams() {
    this(null, null);
  }

  /**
   * @param defaultSortOrder the default sort order.
   */
  public QueryParams(SortOrder defaultSortOrder) {
    this(null, defaultSortOrder);
  }

  /**
   * @param filterExpression Defines a filter expression to be used.
   */
  public QueryParams(QueryExpr filterExpression) {
    this(filterExpression, null);
  }

  /**
   * @param filterExpression Defines a filter expression to be used.
   * @param defaultSortOrder The default sort order.
   */
  public QueryParams(QueryExpr filterExpression, SortOrder defaultSortOrder) {
    this.filterExpression = filterExpression;
    this.defaultSortOrder = defaultSortOrder;
  }

  /**
   * Copies all parameter values from the given source to this instance.
   * <p>
   * Fires the change events {@link #PROP_EFFECTIVE_FILTER} and {@link #PROP_EFFECTIVE_FILTER}.
   *
   * @param src The instance to copy the query parameter values from.
   */
  public void copyParamValues(QueryParams src) {
    // TODO oboede: smart fire of filter change is not yet implemented.
    this.filterExpression = src.getFilterExpression();
    this.baseQueryParams = new HashMap<String, Object>(src.baseQueryParams);
    setSortOrder(src.getSortOrder());
    setDefaultSortOrder(src.getDefaultSortOrder());
    this.execQuery = src.isExecQuery();
    firePropertyChange(PROP_EFFECTIVE_FILTER, null, null);
  }

  /**
   * Sorts the items based on the given {@link SortOrder} definition.
   * <p>
   * <code>null</code> may be passed to switch back to the initial sort order.
   *
   * @param sortOrder
   *          the sort order to use.
   */
  public void setSortOrder(SortOrder sortOrder) {
    SortOrder oldEffectiveOrder = getEffectiveSortOrder();

    this.sortOrder = sortOrder;

    SortOrder newEffectiveOrder = getEffectiveSortOrder();
    if (oldEffectiveOrder != null || newEffectiveOrder != null) {
      firePropertyChange(PROP_EFFECTIVE_SORT_ORDER, oldEffectiveOrder, newEffectiveOrder);
    }
  }

  /**
   * Provides the value that was set with {@link #setSortOrder(SortOrder)}.
   *
   * @return the value.
   */
  public SortOrder getSortOrder() {
    return sortOrder;
  }

  /**
   * Defines the (optional) initial sort order.
   * <p>
   * You may pass <code>null</code> to switch initial sorting off.
   *
   * @param sortOrder
   *          the initial sort order to use.
   */
  public void setDefaultSortOrder(SortOrder sortOrder) {
    SortOrder oldEffectiveOrder = getEffectiveSortOrder();

    this.defaultSortOrder = sortOrder;

    SortOrder newEffectiveOrder = getEffectiveSortOrder();
    firePropertyChange(PROP_EFFECTIVE_SORT_ORDER, oldEffectiveOrder, newEffectiveOrder);
  }

  /**
   * Provides the default sort order definition that is usually used if no other sort order was
   * defined by a {@link #setSortSpec(SortOrder)} call.
   * <p>
   * May be <code>null</code> if there is no sort order defined.
   *
   * @return the default sort order.
   */
  public SortOrder getDefaultSortOrder() {
    return defaultSortOrder;
  }

  /**
   * Provides the currently applied sort order.<br>
   * If no value was set using {@link #setPmSortOrder(SortOrder)}, the current default sort order
   * will be used.<br>
   * May be <code>null</code> if the items are actually not sorted.
   *
   * @return the current sort order.
   */
  public SortOrder getEffectiveSortOrder() {
    return sortOrder != null
        ? sortOrder
        : defaultSortOrder;
  }

  /**
   * Defines a new filter expression to be used.
   *
   * @param expr the new expression. May be <code>null</code> if filtering should be switched off.
   */
  public void setFilterExpression(QueryExpr expr) {
    QueryExpr old = this.filterExpression;
    // TODO: 136039 Vetoable property change does not work with FilterExpressions
    //  try {
    //      fireVetoableChange(PROP_EFFECTIVE_FILTER, old, expr);
    //  } catch (PropertyVetoException e) {
    //      // XXX log here?
    //      return;
    //  }
    this.filterExpression = expr;

    firePropertyChange(PROP_EFFECTIVE_FILTER, old, expr);
  }

  /**
   * Provides the current filter expression.
   *
   * @return the current filter expression. May be <code>null</code>.
   */
  public QueryExpr getFilterExpression() {
    return filterExpression;
  }

  /**
   * Provides the (optional) parameter object for a query the filter is based on.
   *
   * @return the value set by {@link #setBaseQueryParam(Object)}.
   */
  public Object getBaseQueryParam(String name) {
    return baseQueryParams.get(name);
  }

  /**
   * Sets an optional parameter object and fires a property change event for
   * {@link #PROP_EFFECTIVE_FILTER}.
   * <p>
   * This parameter may contain a simple DTO that contains parameters to be used
   * within the query service. This way the query implementation may combine a
   * manual query implementation with generic code that evaluates the filter
   * conditions.
   * <p>
   * If the query needs to be serialized, the query parameter should be
   * serializeable too.
   *
   * @param baseQueryParam
   *          the new parameter. May be <code>null</code>.
   */
  public void setBaseQueryParam(String name, Object baseQueryParam) {
    Object oldBaseQueryParam = this.baseQueryParams.get(name);
    this.baseQueryParams.put(name, baseQueryParam);
    firePropertyChange(PROP_EFFECTIVE_FILTER, oldBaseQueryParam, baseQueryParam);
  }

  /**
   * Allows to switch the query execution on or off.
   * <p>
   * The default value is <code>true</code>.
   *
   * @param execQuery
   *          <code>false</code> causes an empty result set.<br>
   *          <code>true</code> switched usual query execution on.
   */
  public void setExecQuery(boolean execQuery) {
    boolean oldValue = this.execQuery;
    this.execQuery = execQuery;
    firePropertyChange(PROP_EFFECTIVE_FILTER, oldValue, execQuery);
  }

  /**
   * Provides the current query execution switch state.
   *
   * @return <code>true</code> if the query should be executed to find results.
   */
  public boolean isExecQuery() {
    return execQuery;
  }

  @Override
  public QueryParams clone() {
    QueryParams clone = (QueryParams) super.clone();
    return clone;
  }

}
