package org.pm4j.core.pm.filter;

import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.query.QueryParams;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmTable2;

/**
 * A {@link DeprMultiFilter} supports three filter sets with the following logic:
 * <ol>
 *  <li>A set of <b>fix filters</b>:<br>
 *      They get executed for each item and will not be removed by a {@link #clear()} call.<br>
 *      Only items that pass all fix filters will be visible.</li>
 *      Such filters may be used for some fix UI constraints. Example: A table should only display items
 *      defined for a currently selected language.<br>
 *      See also {@link #setFixFilter(String, DeprFilter)}
 *  <li>A set of <b>fix keep visible filters</b>:<br>
 *      They will be executed for items that passed the 'fix filter' checks.<br>
 *      If one of these filters matches, the item will be visible.<br>
 *      They may be used to define some cross cutting filter exceptions.<br>
 *      Example: A filter may define that not yet persisted items should stay
 *      visible, even if the user specified another dynamic filter condition.<br>
 *      See also {@link #setFixKeepVisibleFilter(String, DeprFilter)}
 *  </li>
 *  <li>A set of <b>filters</b>:<br>
 *      They will be executed for all remaining items.<br>
 *      All filters are combined by an AND-logic. That means an item must pass
 *      the checks of all registered filters to get visible.<br>
 *      See also {@link #setFixFilter(String, DeprFilter)}
 *  </li>
 * </ol>
 *
 * Each filter will be added with an identifier.<br>
 * This allows to replace and remove specific filters.
 *
 * @author olaf boede
 *
 * @deprecated see {@link PmTable2} and {@link QueryParams}.
 */
@Deprecated
public class DeprMultiFilter implements DeprFilter {

  /** A set of filters that is not affected by a {@link #clear()} call. */
  private Map<String, DeprFilter> fixFilterMap = new HashMap<String, DeprFilter>();

  /** A set of filters that defines items that stay visible, even if the other filters would hide them.
   *  This map is not affected by a {@link #clear()} call. */
  private Map<String, DeprFilter> fixKeepVisibleFilterMap = new HashMap<String, DeprFilter>();

  /** The 'normal' filter set that will be removed when {@link #clear()} gets called. */
  private Map<String, DeprFilter> filterMap = new HashMap<String, DeprFilter>();

  /** A quick access member for fast filter iteration. */
  private FilterArrays filterArrays = new FilterArrays();

  @Override
  public boolean doesItemMatch(Object item) {
    for (DeprFilter f : filterArrays.activeFixFilters) {
      if (! doesItemMatch(f, item)) {
        return false;
      }
    }

    for (DeprFilter f : filterArrays.activeKeepVisibleFilters) {
      if (doesItemMatch(f, item)) {
        return true;
      }
    }

    for (DeprFilter f : filterArrays.activeFilters) {
      if (! doesItemMatch(f, item)) {
        return false;
      }
    }
    return true;
  }

  /**
   * The {@link DeprMultiFilter} itself takes the items directly (usually PmBean's).
   */
  @Override
  public boolean isBeanFilter() {
    return false;
  }

  /**
   * Activates the given filter.<br>
   * Replaces the filter that was formerly defined for the given id.<br>
   * Removes the filter if <code>null</code> will be passed as
   * <code>filter</code> parameter.
   *
   * @param filterId
   *          The filter identifier.
   * @param filter
   *          The filter to apply.<br>
   *          <code>null</code> removes the filter definition.
   */
  public void setFilter(String filterId, DeprFilter filter) {
    if (filter != null) {
      filterMap.put(filterId, filter);
    }
    else {
      filterMap.remove(filterId);
    }

    updateFilterArrays();
  }

  /**
   * Retrieves the current filter is registered for the given identifier.
   *
   * @param filterId The filter identifier.
   * @return The found filter. Is <code>null</code> if no filter is defined for the given identifier.
   */
  public DeprFilter getFilter(String filterId) {
    return filterMap.get(filterId);
  }

  /**
   * Activates the given fix filter.<br>
   * Replaces the filter that was formerly defined for the given id.<br>
   * Removes the filter if <code>null</code> will be passed as
   * <code>filter</code> parameter.
   *
   * @param filterId
   *          The filter identifier.
   * @param filter
   *          The filter to apply.<br>
   *          <code>null</code> removes the filter definition.
   */
  public void setFixFilter(String filterId, DeprFilter filter) {
    if (filter != null) {
      fixFilterMap.put(filterId, filter);
    }
    else {
      fixFilterMap.remove(filterId);
    }

    updateFilterArrays();
  }

  public DeprFilter getFixFilter(String filterId) {
    return fixFilterMap.get(filterId);
  }

  /**
   * Activates a 'positive' filter. All items with a match to a single 'keep visible' filter
   * will be visible in the result set.<br>
   * Replaces the filter that was formerly defined for the given id.<br>
   * Removes the filter if <code>null</code> will be passed as
   * <code>filter</code> parameter.
   *
   * @param filterId
   *          The filter identifier.
   * @param filter
   *          The filter to apply.<br>
   *          <code>null</code> removes the filter definition.
   */
  public void setFixKeepVisibleFilter(String filterId, DeprFilter filter) {
    if (filter != null) {
      fixKeepVisibleFilterMap.put(filterId, filter);
    }
    else {
      fixKeepVisibleFilterMap.remove(filterId);
    }

    updateFilterArrays();
  }

  public DeprFilter getFixKeepVisibleFilter(String filterId) {
    return fixKeepVisibleFilterMap.get(filterId);
  }

  /**
   * Removes all filter definitions added by calls to {@link #setFilter(String, DeprFilter)}.
   * <p>
   * All fix filters stay untouched.
   */
  public void clear() {
    filterMap.clear();
    updateFilterArrays();
  }

  /**
   * @return <code>true</code> if there is no dynamic filter defined.
   */
  public boolean isEmpty() {
    return filterArrays.empty;
  }

  @SuppressWarnings("unchecked")
  private static final boolean doesItemMatch(DeprFilter f, Object item) {
    return f.isBeanFilter()
        ? f.doesItemMatch(((PmBean<Object>)item).getPmBean())
        : f.doesItemMatch(item);
  }

  private void updateFilterArrays() {
    filterArrays = new FilterArrays();
  }

  private class FilterArrays {
    final DeprFilter[] activeFixFilters         = fixFilterMap.values().toArray(new DeprFilter[fixFilterMap.size()]);
    final DeprFilter[] activeKeepVisibleFilters = fixKeepVisibleFilterMap.values().toArray(new DeprFilter[fixKeepVisibleFilterMap.size()]);
    final DeprFilter[] activeFilters            = filterMap.values().toArray(new DeprFilter[filterMap.size()]);
    final boolean  empty                    = fixFilterMap.isEmpty() && fixKeepVisibleFilterMap.isEmpty() && filterMap.isEmpty();
  }
}
