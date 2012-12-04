package org.pm4j.core.pm;

import java.util.Collection;
import java.util.List;

import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.core.pm.PmTable.RowSelectMode;
import org.pm4j.core.pm.impl.changehandler.ChangeSetHandler;


/**
 * PM for tables.<br>
 * A table provides columns (@see {@link #getColumns()}) and rows (see {@link #getTotalNumOfRows()}).
 *
 * @author OBOEDE
 *
 * @param <T_ROW_PM> The type used for rows.
 */
public interface PmTable2<T_ROW_PM> extends PmObject, PmDataInput {

  /** Identifer for things that may be cleared by calling {@link PmTable2#updatePmTable(UpdateAspect...)} */
  public enum UpdateAspect {
    /** Switches back to the default sort order. */
    CLEAR_SORT_ORDER,
    /** Clears user defined filters. */
    CLEAR_USER_FILTER,
    /** Clears the selection. */
    CLEAR_SELECTION,
    /** Clears any registered changed-data states. */
    CLEAR_CHANGES
  }

  /**
   * @return The set of columns.
   */
  List<PmTableCol> getColumnPms();

  /** @deprecated Please use {@link #getColumnPms()} */
  List<PmTableCol> getColumns();

  /**
   * Provides only the visible rows.<br>
   * The provided set may be influenced by filter criteria and paging logic.
   * <p>
   * Does not provide any items if the table is not visible.
   *
   * @return The set of table rows to display.
   */
  List<T_ROW_PM> getRowPms();

  /** @deprecated Please use {@link #getRowPms()} */
  List<T_ROW_PM> getRows();

  /**
   * Provides a row representation that may be used by generic a renderer.
   * <p>
   * Does not provide any items if the table is not visible.
   *
   * @return The set of rows as provides by {@link #getRows()}.<br>
   *         Each row is encapsulated in a {@link PmTableGenericRow} instance.
   */
  List<PmTableGenericRow2<T_ROW_PM>> getGenericRowPms();

  /**
   * @return The number of rows per table page.
   */
  int getNumOfPageRowPms();

  /** @deprecated Please use {@link #getNumOfPageRowPms()} */
  int getNumOfPageRows();

  /**
   * @return The total size of the un-filtered row set behind this table PM.
   */
  long getTotalNumOfPmRows();

  /** @deprecated Please use {@link #getNumOfPageRowPms()} */
  int getTotalNumOfRows();

  /**
   * Provides all selection related operations.
   *
   * @return the {@link SelectionHandler}.
   */
  SelectionHandler<T_ROW_PM> getPmSelectionHandler();

  /**
   * Provides the single currently active row. This information is usually used
   * in master-details scenarios.
   * <p>
   * In case or {@link RowSelectMode#SINGLE} this is usually the selected row.<br>
   * In case of {@link RowSelectMode#MULTI} the default implementation returns
   * <code>null</code>.
   * <p>
   * Sub classes with a different 'current row' definition logic
   * may provide alternate implementations by overriding this method.
   *
   * @return The currently active (selected) row or <code>null</code> if none is
   *         active.
   */
  T_ROW_PM getCurrentRowPm();

  /**
   * @return the query behind this table.
   */
  QueryParams getPmQueryParams();

  /**
   * @return the query options that can be offered to the user to define query constraints.
   */
  QueryOptions getPmQueryOptions();

  /** The set of table changes that can be considered. */
  enum TableChange { SELECTION, FILTER, PAGE, SORT }

  /**
   * Adds a change decorator for table changes.
   * <p>
   * A table change can be prevented by the before-do logic of the decorator.
   *
   * @param decorator
   *          The decorator to add.
   * @param changes
   *          The set of aspects to apply the decorator for. If no change aspect
   *          is passed, the decorator will be used for all table change
   *          aspects.
   */
  void addPmDecorator(PmCommandDecorator decorator, TableChange... changes);

  /**
   * Provides the decorators to consider for a given change kind.
   *
   * @param change The change kind.
   * @return The decorators, defined for the given change kind.
   */
  Collection<PmCommandDecorator> getPmDecorators(TableChange change);

  /**
   * Provides the change set handler.<br>
   * It provides information about table changes.
   *
   * @return the table change registry.
   */
  ChangeSetHandler<T_ROW_PM> getPmChangeSetHandler();

  /**
   * Clears a defined set of table state aspects.
   *
   * @param updateAspect the updates to do. If no value is passed, all updates will be done.
   */
  void updatePmTable(UpdateAspect... updateAspect);

  /**
   * PM for table with a pager.
   *
   * @param <T_ROW_PM> The type used for row objects.
   */
  public static interface WithPager<T_ROW_ELEMENT> extends PmTable2<T_ROW_ELEMENT>{

    /**
     * @return The pager used for the table.
     */
    PmPager getPmPager();

    /** @deprecated Please use {@link #getPmPager()} */
    PmPager getPager();
  }

}
