package org.pm4j.core.pm;

import org.pm4j.core.util.table.ColSizeSpec;

/**
 * PM of a table column.
 *
 * @author Olaf Boede
 */
public interface PmTableCol extends PmObject {

  /**
   * @return The column size specification.<br>
   *         May be <code>null</code> if there is no size specified for the
   *         column.
   */
  ColSizeSpec getPmColSize();

  /**
   * Specification and visualization of the column sort order is supported by
   * the PM attribute provided by this method.
   *
   * @return The attribute that defines the column sort order.
   */
  PmAttrEnum<PmSortOrder> getSortOrderAttr();

  /**
   * @return A command that allows to switch the sort order.
   */
  PmCommand getCmdSort();

}
