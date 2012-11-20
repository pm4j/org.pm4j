package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;

public class QueryUtil {

  public static FilterCompareDefinition findFilterDefinitionByName(QueryOptions queryOptions, String name) {
    for (FilterCompareDefinition d : queryOptions.getCompareDefinitions()) {
      if (StringUtils.equals(d.getAttr().getName(), name)) {
        return d;
      }
    }
    // not found
    return null;
  }

  public static CompOp findCompOp(FilterCompareDefinition d, String opName) {
    for (CompOp c : d.getCompOps()) {
      if (StringUtils.equals(c.getName(), opName)) {
        return c;
      }
    }
    // not found
    return null;
  }

  public static FilterExpression getFilter(QueryOptions queryOptions, String predicateName, String compOpName, Object value) {
    FilterCompareDefinition d = findFilterDefinitionByName(queryOptions, predicateName);
    if (d == null) {
      throw new RuntimeException("Missing filter definition for attribute '" + predicateName + "'");
    }

    CompOp c = findCompOp(d, compOpName);
    if (c == null) {
      throw new RuntimeException("Missing compare operator definition '" + compOpName + "' in filter definition '" + predicateName + "'.");
    }

    return new FilterCompare(d.getAttr(), c, value);
  }
}
