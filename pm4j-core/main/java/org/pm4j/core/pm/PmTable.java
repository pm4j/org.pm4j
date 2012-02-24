package org.pm4j.core.pm;

import java.util.List;

import org.pm4j.core.pm.pageable.PmPager;

/**
 * PM for tables.<br>
 * A table provides columns (@see {@link #getColumns()}) and rows (see {@link #getTotalNumOfRows()}).
 *
 * @author OBOEDE
 *
 * @param <T_ROW_OBJ> The type used for row objects.
 */
public interface PmTable<T_ROW_OBJ> extends PmObject, PmDataInput {

  /**
   * The set of supported row selection modes.
   */
  public static enum RowSelectMode {
    /** Only a single row may be selected. */
    SINGLE,
    /** More than one row may be selected. */
    MULTI,
    /** No row can be marked as selected. */
    NO_SELECTION,
    /** This value defines no specific mode. The default should be applied. */
    DEFAULT
  }


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
   * @return The set of rows that contains changed data.
   */
  List<T_ROW_OBJ> getRowsWithChanges();


  /**
   * Provides a row representation that may be used by generic a renderer.
   *
   * @return The set of rows as provides by {@link #getRows()}.<br>
   *         Each row is encapsulated in a {@link PmTableGenericRow} instance.
   */
  List<PmTableGenericRow<T_ROW_OBJ>> getGenericRows();

  /**
   * @return The number of rows per table page.
   */
  int getNumOfPageRows();

  /**
   * @return The total size of the un-filtered row set.
   */
  int getTotalNumOfRows();

  /**
   * @return The row selection mode for this table.
   */
  RowSelectMode getRowSelectMode();

  /**
   * @return <code>true</code> if the table supports the selection of multiple rows.
   * @deprecated Please use {@link #getRowSelectMode()}
   */
  @Deprecated
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
    PmPager getPager();
  }

}
