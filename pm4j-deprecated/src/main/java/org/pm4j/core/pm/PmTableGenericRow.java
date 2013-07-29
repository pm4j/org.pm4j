package org.pm4j.core.pm;

import java.util.List;

/**
 * Provides generic table row information.
 * <p>
 * The row cells may be obtained as a list or by index.<br>
 * The order of cells corresponds to the current table column index sequence. See {@link PmTableCol#getColPosAttr()}.
 *
 * @author olaf boede
 *
 * @param <T_BACKING_ROW_OBJ>
 */
public interface PmTableGenericRow<T_BACKING_ROW_OBJ> {

  /**
   * @return The table this row belongs to.
   */
  PmTable<T_BACKING_ROW_OBJ> getPmTable();

  /**
   * @return The sorted set of row cells.
   */
  List<PmObject> getCells();

  /**
   * @param colIdx Position index of the column to get the cell for.
   * @return The corresponding row cell.
   */
  PmObject getCell(int colIdx);

  /**
   * Row content is based on a data object.<br>
   * This method provides access to the related row data object.
   *
   * @return The object that provides the row content.
   */
  T_BACKING_ROW_OBJ getBackingBean();

}
