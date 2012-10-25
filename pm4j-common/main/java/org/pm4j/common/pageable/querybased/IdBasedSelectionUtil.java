package org.pm4j.common.pageable.querybased;

import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterCompare;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterNot;

public class IdBasedSelectionUtil {

  /**
   * Generates a filter expression based on a query filter restriction and a set
   * of {@link ClickedIds}.
   *
   * @param idAttr
   *          the ID attribute that is related to the clicked id's.
   * @param queryFilterExpr
   *          the (optional) query filter which is used in case of inverted
   *          selections.
   * @param clickedIds
   *          the set of individually selected/deselected item ids.
   * @return the generated filter restriction.<br>
   *         May provide a <code>null</code>. In this case there is no filter
   *         restriction. Which means that all items match.
   */
  public static <T_ID> FilterExpression makeSelectionFilterExpression(AttrDefinition idAttr, FilterExpression queryFilterExpr, ClickedIds<T_ID> clickedIds) {
    // no clicked id:
    if (clickedIds.getIds().isEmpty()) {
      return clickedIds.isInvertedSelection()
          // no 'inverted' items selected -> the complete query filter result is selected.
          ? queryFilterExpr
          // empty positive selection -> a 'false' filter expression to get no matches.
          // TODO olaf: a real false-filter is missing.
          : new FilterCompare(idAttr, new CompOpIsNull(), null);
    }
    // handle clicked ids:
    else {
      FilterExpression idFilterExpr = new FilterCompare(idAttr, new CompOpIn(), clickedIds.getIds());
      if (clickedIds.isInvertedSelection()) {
        idFilterExpr = new FilterNot(idFilterExpr);
      }

      return (queryFilterExpr != null)
        ? new FilterAnd(queryFilterExpr, idFilterExpr)
        : idFilterExpr;
    }
  }

}
