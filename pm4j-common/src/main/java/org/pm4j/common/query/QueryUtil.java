package org.pm4j.common.query;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.filter.FilterDefinition;

/**
 * Utilities for the abstract query functionality.
 *
 * @author Olaf Boede
 */
public class QueryUtil {

  /**
   * Registers the default {@link CompOp} to value type matches that most
   * evaluators may support.
   *
   * @param checker The checker to register the matches for.
   * @return The checker again for fluent programming style.
   */
  public static CompOpCompatibilityChecker registerDefaultCompOpValueMatches(CompOpCompatibilityChecker checker) {
    checker.registerCompOp(CompOpContains.class, String.class);
    checker.registerCompOp(CompOpEquals.class, Object.class);
    checker.registerCompOp(CompOpGe.class, Comparable.class);
    checker.registerCompOp(CompOpGt.class, Comparable.class);
    checker.registerCompOp(CompOpIn.class, Collection.class);
    checker.registerCompOp(CompOpIsNull.class, Object.class);
    checker.registerCompOp(CompOpLe.class, Comparable.class);
    checker.registerCompOp(CompOpLike.class, String.class);
    checker.registerCompOp(CompOpLt.class, Comparable.class);
    checker.registerCompOp(CompOpNotContains.class, String.class);
    checker.registerCompOp(CompOpNotEquals.class, Object.class);
    checker.registerCompOp(CompOpNotNull.class, Object.class);
    checker.registerCompOp(CompOpStartsWith.class, String.class);
    return checker;
  }

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
