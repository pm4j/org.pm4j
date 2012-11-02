package org.pm4j.core.pm.impl;

import java.util.List;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmPager2.PagerVisibility;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.impl.changehandler.MasterPmHandler;

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
    List<?> rows = tablePm.getRows();
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
    table.setNumOfPageRows(Integer.MAX_VALUE);
    if (table.getPmPager() != null) {
      table.getPmPager().setPagerVisibility(PagerVisibility.WHEN_SECOND_PAGE_EXISTS);
    }
  }

  /**
   * Provides the corresponding cell PM for a column within the given row.
   * <p>
   * The implementation of <code>PmTableColImpl#findCorrespondingRowCell</code>
   *
   * @param column
   *          The column to find a row cell for.
   * @param rowElement
   *          PM of the row that contains the cell.
   * @return The found cell PM.
   * @throws PmRuntimeException
   *           if there is no corresponding cell.
   */
  public static PmObject getRowCellForTableCol(PmTableCol column, PmElement rowElement) {
    PmObject pm = ((PmTableColImpl) column).findCorrespondingRowCell(rowElement);
    if (pm == null) {
      throw new PmRuntimeException(column, "No corresponding table row cell PM found in row: " + rowElement);
    }
    return pm;
  }

  /**
   * Adds a master-details change handler to a table that should know about the changes
   * reported by the change handler.
   *
   * @param pmTable
   *          The table that should consider the details changes.
   * @param handler
   *          The handler that reports changes that are related to the content
   *          handled within the table.
   * @return The passed handler (for inline code style support).
   */
  public static <T extends MasterPmHandler> T addMasterDetailsPmHandler(PmTable2<?> pmTable, T handler) {
    PmTableImpl2<?, ?> pmTableImpl = (PmTableImpl2<?, ?>) pmTable;
    pmTableImpl.getPmChangeSetHandler().addDetailsPmHandler(handler);
    handler.startObservers();
    return handler;
  }

}
