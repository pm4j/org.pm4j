package org.pm4j.common.query;

import java.util.List;


/**
 * Provides the set of query restrictions that can be used to configure a {@link Query}.
 *
 * @author olaf boede
 */
public interface QueryOptions {

  /**
   * Provides the sort order for the given attribute.
   * <p>
   * Is usually used by a column that asks if it is sortable.
   *
   * @param attrName name
   * @return the corresponding sort order definition or <code>null</code>.
   */
  SortOrder getSortOrder(String attrName);

  /**
   * Provides the default sort order.
   *
   * @return the default sort order or <code>null</code> is none is defined.
   */
  SortOrder getDefaultSortOrder();

  /**
   * Provides the set of available compare definitions.
   * <p>
   * Usually this is used to provide the filter options the user may configure.
   *
   * @return the filter definition. Returns never <code>null</code>.
   */
  List<FilterCompareDefinition> getCompareDefinitions();

}
