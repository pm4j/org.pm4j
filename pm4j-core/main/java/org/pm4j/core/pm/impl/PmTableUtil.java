package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmPager.PagerVisibility;

/**
 * Some table related helper functions.
 *
 * @author olaf boede
 */
public final class PmTableUtil {

    /**
     * Defines the properties of the given table as needed for scrollable (not pageable) tables.
     *
     * @param table
     *            The table to adjust.
     */
    public static void setScrollableTableProperties(PmTableImpl<?> table) {
        table.setNumOfPageRows(Integer.MAX_VALUE);
        if (table.getPager() != null) {
          table.getPager().setPagerVisibility(PagerVisibility.WHEN_SECOND_PAGE_EXISTS);
        }
    }

    /**
     * Provides the corresponding cell PM for a column within the given row.
     * <p>
     * The implementation of <code>PmTableColImpl#findCorrespondingRowCell</code>
     *
     * @param column The column to find a row cell for.
     * @param rowElement PM of the row that contains the cell.
     * @return The found cell PM.
     * @throws PmRuntimeException if there is no corresponding cell.
     */
    public static PmObject getRowCellForTableCol(PmTableCol column, PmElement rowElement) {
      PmObject pm = ((PmTableColImpl)column).findCorrespondingRowCell(rowElement);
      if (pm == null) {
        throw new PmRuntimeException(column, "No corresponding table row cell PM found in row: " + rowElement);
      }
      return pm;
    }

}
