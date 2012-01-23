package org.pm4j.core.pm;

import java.util.List;

import org.pm4j.core.pm.pageable.PmPager;

/**
 * PM for tables.<br>
 * A table provides columns (@see {@link #getColumns()}) and rows (see {@link #getRowNum()}).
 *
 * @author OBOEDE
 *
 * @param <T_ROW_OBJ> The type used for row objects.
 */
public interface PmTable<T_ROW_OBJ> extends PmObject, PmDataInput {

  /**
   * @return The set of columns.
   */
  List<PmTableCol> getColumns();

  /**
   * Provides only the visible rows.<br>
   * The provided set may be influenced by filter criteria and paging logic.
   *
   * @return The set of table rows to display.
   */
  List<T_ROW_OBJ> getRows();

  /**
   * Provides a row representation that may be used by generic a renderer.
   *
   * @return The set of rows as provides by {@link #getRows()}.<br>
   *         Each row is encapsulated in a {@link PmTableGenericRow} instance.
   */
  List<PmTableGenericRow<T_ROW_OBJ>> getGenericRows();

  /**
   * @return The total number of rows to display within this table.
   */
  int getRowNum();

  /**
   * @return <code>true</code> if the table supports the selection of multiple rows.
   */
  boolean isMultiSelect();

  /**
   * PM for table with a pager.
   *
   * @param <T_ROW_OBJ> The type used for row objects.
   */
  public static interface WithPager<T_ROW_ELEMENT> extends PmTable<T_ROW_ELEMENT>{

    /**
     * @return The pager used for the table.
     */
    PmPager<T_ROW_ELEMENT> getPager();
  }

}
