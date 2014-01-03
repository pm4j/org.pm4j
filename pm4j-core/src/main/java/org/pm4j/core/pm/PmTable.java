package org.pm4j.core.pm;

import java.util.Collection;
import java.util.List;

import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.selection.SelectionHandler;


/**
 * PM for tables.<br>
 * A table provides columns (@see {@link #getColumnPms()}) and rows (see {@link #getRowPms()}).
 *
 * @author OBOEDE
 *
 * @param <T_ROW_PM> The type used for rows.
 */
public interface PmTable<T_ROW_PM> extends PmObject, PmDataInput {

  /** Identifier for things that may be cleared by calling {@link PmTable#updatePmTable(UpdateAspect...)} */
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
   * Provides the set of rows displayed on the current page.<br>
   * The provided set may be influenced by filter criteria and paging logic.
   *
   * @return The set of table rows to display.
   */
  List<T_ROW_PM> getRowPms();

  /** @deprecated Please use {@link #getRowPms()} */
  List<T_ROW_PM> getRows();

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
   * Provides a handler for all selection related operations.
   *
   * @return the {@link SelectionHandler}.
   */
  SelectionHandler<T_ROW_PM> getPmSelectionHandler();

  /**
   * Provides the single currently active master row. This information is usually used
   * in master-details scenarios.
   * <p>
   * In case or {@link RowSelectMode#SINGLE} this is usually the selected row.<br>
   * In case of {@link RowSelectMode#MULTI} the default implementation returns
   * <code>null</code>.
   * <p>
   * Sub classes with a different 'master row' definition logic
   * may provide alternate implementations by overriding this method.
   *
   * @return The currently active (selected) master row or <code>null</code> if none is
   *         active.
   */
  T_ROW_PM getMasterRowPm();

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
   * @return The {@link PageableCollection} that handles the table row PM's to display.
   */
  PageableCollection<T_ROW_PM> getPmPageableCollection();

  /**
   * Clears a defined set of table state aspects.
   *
   * @param updateAspect the updates to do. If no value is passed, all updates will be done.
   */
  void updatePmTable(UpdateAspect... updateAspect);

  // --- Implementation details offered to other PM implementations ---

  /** @return Implementation details offered to other PM implementations. */
  ImplDetails getPmImplDetails();

  interface ImplDetails {
    /** @return <code>true</code> if this table is configured to have all columns sortable by default. */
    boolean isSortable();
  }

}
