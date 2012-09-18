package org.pm4j.core.pm.filter;

import java.util.HashMap;
import java.util.Map;

import org.pm4j.core.pm.PmBean;

/**
 * A filter that is a composition of multiple filters.<br>
 * All sub-filters are combined by an AND-logic. That means an item must pass
 * the checks of all registered filters to get visible.
 * <p>
 * But: If an item matches a 'keep visible' filter definition it also stays visible.
 * All 'keep visible' filters are combined by an OR-logic.
 * <p>
 * Each sub-filter can be added with an identifier.<br>
 * This allows to replace and remove specific filters.
 *
 * @author olaf boede
 */
public class MultiFilter implements Filter {

  /** The 'normal' filter set that will be removed when {@link #clear()} gets called. */
  private Map<String, Filter> filterMap = new HashMap<String, Filter>();

  /** A set of filters that is not affected by a {@link #clear()} call. */
  private Map<String, Filter> fixFilterMap = new HashMap<String, Filter>();

  /** A set of filters that defines items that stay visible, even if the other filters would hide them.
   *  This map is not affected by a {@link #clear()} call. */
  private Map<String, Filter> fixKeepVisibleFilterMap = new HashMap<String, Filter>();

  private Filter[] activeKeepVisibleFilters = {};
  private Filter[] activeFilters = {};

  @Override
  public boolean doesItemMatch(Object item) {
    for (Filter f : activeKeepVisibleFilters) {
      if (doesItemMatch(f, item)) {
        return true;
      }
    }

    for (Filter f : activeFilters) {
      if (! doesItemMatch(f, item)) {
        return false;
      }
    }
    return true;
  }

  /**
   * The {@link MultiFilter} itself takes the items directly (usually PmBean's).
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
  public void setFilter(String filterId, Filter filter) {
    if (filter != null) {
      filterMap.put(filterId, filter);
    }
    else {
      filterMap.remove(filterId);
    }

    updateActiveFilters();
  }

  /**
   * Retrieves the current filter is registered for the given identifier.
   *
   * @param filterId The filter identifier.
   * @return The found filter. Is <code>null</code> if no filter is defined for the given identifier.
   */
  public Filter getFilter(String filterId) {
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
  public void setFixFilter(String filterId, Filter filter) {
    if (filter != null) {
      fixFilterMap.put(filterId, filter);
    }
    else {
      fixFilterMap.remove(filterId);
    }

    updateActiveFilters();
  }

  public Filter getFixFilter(String filterId) {
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
  public void setFixKeepVisibleFilter(String filterId, Filter filter) {
    if (filter != null) {
      fixKeepVisibleFilterMap.put(filterId, filter);
    }
    else {
      fixKeepVisibleFilterMap.remove(filterId);
    }

    updateActiveFilters();
  }

  public Filter getFixKeepVisibleFilter(String filterId) {
    return fixKeepVisibleFilterMap.get(filterId);
  }

  /**
   * Removes all filter definitions added by calls to {@link #setFilter(String, Filter)}.
   * <p>
   * All fix filters stay untouched.
   */
  public void clear() {
    filterMap.clear();
    updateActiveFilters();
  }

  /**
   * @return <code>true</code> if there is no filter defined.
   */
  public boolean isEmpty() {
    return activeFilters.length == 0;
  }

  @SuppressWarnings("unchecked")
  private static final boolean doesItemMatch(Filter f, Object item) {
    return f.isBeanFilter()
        ? f.doesItemMatch(((PmBean<Object>)item).getPmBean())
        : f.doesItemMatch(item);
  }

  private void updateActiveFilters() {
    int size = fixFilterMap.size() + filterMap.size();
    Filter[] activeFilters = new Filter[size];

    int i = 0;
    for (Filter f : fixFilterMap.values()) { activeFilters[i++] = f; }
    for (Filter f : filterMap.values()) { activeFilters[i++] = f; }

    Filter[] keepVisibleFilters = new Filter[fixKeepVisibleFilterMap.size()];
    i = 0;
    for (Filter f : fixKeepVisibleFilterMap.values()) { keepVisibleFilters[i++] = f; }

    this.activeKeepVisibleFilters = keepVisibleFilters;
    this.activeFilters = activeFilters;
  }

}
