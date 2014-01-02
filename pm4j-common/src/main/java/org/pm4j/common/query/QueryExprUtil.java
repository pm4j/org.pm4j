package org.pm4j.common.query;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.filter.FilterDefinition;

/**
 * Helper methods that support some common {@link QueryExpr} related operations.
 *
 * @author olaf boede
 */
public final class QueryExprUtil {

  /**
   * Creates a {@link QueryExprCompare} expression.<br>
   * Uses the type of the given <code>compareValue</code> to identify the attriubute type.
   *
   * @param attrPath
   * @param compOpClass
   * @param compareValue
   * @return
   */
  public static final QueryExprCompare makeCompareExpr(String attrPath, Class<? extends CompOp> compOpClass, Object compareValue) {
    Class<?> attrClass = compareValue != null ? compareValue.getClass() : Object.class;
    QueryAttr attr = new QueryAttr(attrPath, attrClass);
    return new QueryExprCompare(attr, compOpClass, compareValue);
  }

  /**
   * Scans the given collection of {@link FilterDefinition}s to find the
   * first one that defines a condition for an attribute with the given
   * attribute path name.
   *
   * @param fcds
   *          the set of filter {@link FilterDefinition}s to scan.
   * @param name
   *          the attribute path name to find a defintion for.
   * @return the found definition or <code>null</code>.
   */
  public static FilterDefinition findFilterCompareDefinitionByPathName(Collection<FilterDefinition> fcds, String name) {
    for (FilterDefinition fcd : fcds) {
      if (StringUtils.equals(fcd.getAttr().getPath(), name)) {
        return fcd;
      }
    }
    // not found
    return null;
  }

}
