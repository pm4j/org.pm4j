package org.pm4j.common.query;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Helper methods that support some common {@link FilterExpression} related operations.
 *
 * @author olaf boede
 */
public final class FilterUtil {

  /**
   * Creates a {@link FilterCompare} expression.<br>
   * Uses the type of the given <code>compareValue</code> to identify the attriubute type.
   *
   * @param attrPath
   * @param compOpClass
   * @param compareValue
   * @return
   */
  public static final FilterCompare makeCompareExpr(String attrPath, Class<? extends CompOp> compOpClass, Object compareValue) {
    Class<?> attrClass = compareValue != null ? compareValue.getClass() : Object.class;
    QueryAttr attr = new QueryAttr(attrPath, attrClass);
    return new FilterCompare(attr, compOpClass, compareValue);
  }

  /**
   * Scans the given collection of {@link FilterCompareDefinition}s to find the
   * first one that defines a condition for an attribute with the given
   * attribute path name.
   *
   * @param fcds
   *          the set of filter {@link FilterCompareDefinition}s to scan.
   * @param name
   *          the attribute path name to find a defintion for.
   * @return the found definition or <code>null</code>.
   */
  public static FilterCompareDefinition findFilterCompareDefinitionByPathName(Collection<FilterCompareDefinition> fcds, String name) {
    for (FilterCompareDefinition fcd : fcds) {
      if (StringUtils.equals(fcd.getAttr().getPath(), name)) {
        return fcd;
      }
    }
    // not found
    return null;
  }

}
