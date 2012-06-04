package org.pm4j.core.pm.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * A filter that is a composition of multiple filters.<br>
 * All sub-filters are combined by an AND-logic. That means an item must pass
 * the checks of all registered filters to get visible.
 * <p>
 * Each sub-filter can be added with an identifier.<br>
 * This allows to replace and remove specific filters.
 *
 * @author olaf boede
 */
public class MultiFilter implements Filter {

  private Map<String, Filter> filterMap = new HashMap<String, Filter>();
  private Filter[] activeFilters = {};

  @Override
  public boolean doesItemMatch(Object item) {
    for (Filter f : activeFilters) {
      if (!f.doesItemMatch(item)) {
        return false;
      }
    }
    return true;
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
   * @return <code>true</code>.
   */
  public void setFilter(String filterId, Filter filter) {
    if (filter != null) {
      filterMap.put(filterId, filter);
    }
    else {
      filterMap.remove(filterId);
    }

    activeFilters = filterMap.values().toArray(new Filter[filterMap.size()]);
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
   * Removes all filter definitions.
   */
  public void clear() {
    filterMap.clear();
    activeFilters = new Filter[0];
  }

  /**
   * @return <code>true</code> if there is no filter defined.
   */
  public boolean isEmpty() {
    return activeFilters.length == 0;
  }
}
