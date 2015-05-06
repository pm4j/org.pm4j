package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
   * Only for expression having an attribute of type {@link QueryAttrMulti}!
   * <p>
   * Generates the related AND or OR combined query expression.
   *
   * @param expr
   * @return
   */
  public static QueryExpr makeMultiPartCompareExpr(QueryExprCompare expr) {
    QueryAttrMulti multiAttr = ((QueryAttrMulti) expr.getAttr());
    List<QueryExpr> partExprList = new ArrayList<QueryExpr>(multiAttr.getParts().size());
    for (QueryAttr a : multiAttr.getParts()) {
      partExprList.add(new QueryExprCompare(a, expr.getCompOp(), expr.getValue()));
    }
    return multiAttr.isOrCombined()
        ? new QueryExprOr(partExprList)
        : new QueryExprAnd(partExprList);
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
