package org.pm4j.core.pm.impl;

import java.util.List;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;

/**
 * Some table related helper functions.
 *
 * @author Olaf Boede
 */
public final class PmTableUtil2 {

  /**
   * Provides the index of the given row object within it's table.
   *
   * @param rowPm The PM of the row to check.
   * @return The index (first item has the index zero).<br>
   *    <code>-1</code> if the given row is not on the current page.
   */
  public static int findIndexOfRowOnCurrentPage(PmObject rowPm) {
    if (rowPm.getPmParent() instanceof PmTable) {
      PmTable<?> tablePm = (PmTable<?>) rowPm.getPmParent();
      List<?> rows = tablePm.getRowPms();
      for (int i = 0; i < rows.size(); ++i) {
        if (rows.get(i) == rowPm) {
          return i;
        }
      }
    }

    // row not on current page
    return -1;
  }

}
