package org.pm4j.core.pm.impl;

import java.util.List;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmPager2.PagerVisibility;
import org.pm4j.core.pm.PmTable2;

/**
 * Some table related helper functions.
 *
 * @author olaf boede
 */
public final class PmTableUtil2 {

  /**
   * Provides the index of the given row object within it's table.
   *
   * @param rowPm The PM of the row to check.
   * @return The index (first item has the index zero).<br>
   *    <code>-1</code> if the given row is not on the current page.
   */
  public static int findIndexOfRowOnCurrentPage(PmElement rowPm) {
    PmTable2<?> tablePm = PmUtil.getPmParentOfType(rowPm, PmTable2.class);
    List<?> rows = tablePm.getRowPms();
    for (int i = 0; i < rows.size(); ++i) {
      if (rows.get(i) == rowPm) {
        return i;
      }
    }
    // row not on current page
    return -1;
  }

  /**
   * Defines the properties of the given table as needed for scrollable (not
   * pageable) tables.
   *
   * @param table
   *          The table to adjust.
   */
  public static void setScrollableTableProperties(PmTableImpl2<?, ?> table) {
    table.setNumOfPageRowPms(Integer.MAX_VALUE);
    if (table.getPmPager() != null) {
      table.getPmPager().setPagerVisibility(PagerVisibility.WHEN_SECOND_PAGE_EXISTS);
    }
  }

  /**
   * @deprecated please use the table's in-memory collection support directly.
   *
   * @param pmTable
   * @return
   */
  public static QueryOptions makeQueryOptionsForInMemoryTable(PmTableImpl2<?, ?> pmTable) {
    return pmTable.new InMemQueryOptionProvider().getQueryOptions();
  }


}
