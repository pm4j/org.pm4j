package org.pm4j.common.query;

import java.util.List;


/**
 * Provides the set of query restrictions that can be used to configure a {@link Query}.
 *
 * @author olaf boede
 */
public interface QueryOptions {

  SortOrder getSortOrder(String attrName);
  SortOrder getDefaultSortOrder();

  List<FilterCompareDefinition> getCompareDefinitions();

}
