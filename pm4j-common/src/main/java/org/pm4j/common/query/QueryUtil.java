package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.filter.FilterDefinition;

/**
 * Utilities for the abstract query functionality.
 *
 * @author Olaf Boede
 */
public class QueryUtil {

  public static FilterDefinition findFilterDefinitionByName(QueryOptions queryOptions, String name) {
    for (FilterDefinition d : queryOptions.getCompareDefinitions()) {
      if (StringUtils.equals(d.getAttr().getName(), name)) {
        return d;
      }
    }
    // not found
    return null;
  }

  public static CompOp findCompOp(FilterDefinition d, String opName) {
    for (CompOp c : d.getCompOps()) {
      if (StringUtils.equals(c.getName(), opName)) {
        return c;
      }
    }
    // not found
    return null;
  }

  public static QueryExpr getFilter(QueryOptions queryOptions, String predicateName, String compOpName, Object value) {
    FilterDefinition d = findFilterDefinitionByName(queryOptions, predicateName);
    if (d == null) {
      throw new RuntimeException("Missing filter definition for attribute '" + predicateName + "'");
    }

    CompOp c = findCompOp(d, compOpName);
    if (c == null) {
      throw new RuntimeException("Missing compare operator definition '" + compOpName + "' in filter definition '" + predicateName + "'.");
    }

    return new QueryExprCompare(d.getAttr(), c, value);
  }
}
