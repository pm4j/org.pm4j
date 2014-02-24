package org.pm4j.deprecated.core.pm;

import java.util.Collection;
import java.util.List;

import org.pm4j.common.selection.SelectMode;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.deprecated.core.pm.filter.DeprFilterByDefinition;
import org.pm4j.deprecated.core.pm.filter.DeprFilterByDefinitionProvider;
import org.pm4j.deprecated.core.pm.filter.DeprFilterable;


/**
 * PM for tables.<br>
 * A table provides columns (@see {@link #getColumns()}) and rows (see {@link #getTotalNumOfRows()}).
 *
 * @author OBOEDE
 *
 * @param <T_ROW_OBJ> The type used for row objects.
 *
 * @deprecated Please use {@link PmTable}.
 */
@Deprecated
public interface DeprPmTable<T_ROW_OBJ> extends PmObject, PmDataInput, DeprFilterable, DeprFilterByDefinitionProvider {

  /**
   * The set of supported row selection modes.
   */
  // TODO olaf: remove after switch to new table implementation.
  public static enum RowSelectMode {
    /** Only a single row may be selected. */
    SINGLE,
    /** More than one row may be selected. */
    MULTI,
    /** No row can be marked as selected. */
    NO_SELECTION,
    /** This value defines no specific mode. The default should be applied. */
    DEFAULT;

    public SelectMode toSelectMode() {
      switch (this) {
        case SINGLE: return SelectMode.SINGLE;
        case MULTI: return SelectMode.MULTI;
        case NO_SELECTION: return SelectMode.NO_SELECTION;
        default: return SelectMode.DEFAULT;
      }
    }
  }


  /**
   * @return The set of columns.
   */
  List<DeprPmTableCol> getColumns();

  /**
   * Provides only the visible rows.<br>
   * The provided set may be influenced by filter criteria and paging logic.
   *
   * @return The set of table rows to display.
   */
  List<T_ROW_OBJ> getRows();

  /**
   * Provides only the visible rows.<br>
   * The provided set may be influenced by filter criteria and paging logic.
   *
   * @return The set of table rows to display.
   */
  List<T_ROW_OBJ> getRowPms();

  /**
   * @return The set of current filter definitions that can be
   *         specified/modified for this table.<br>
   *         Returns never <code>null</code>.
   */
  List<DeprFilterByDefinition> getFilterByDefinitions();

  /**
   * @return The set of rows containing changed data.
   */
  List<T_ROW_OBJ> getRowsWithChanges();

  /**
   * Clear set of rows containing changed data.
   */
  void clearRowsWithChanges();

  /**
   * Provides a row representation that may be used by generic a renderer.
   *
   * @return The set of rows as provides by {@link #getRows()}.<br>
   *         Each row is encapsulated in a {@link DeprPmTableGenericRow} instance.
   */
  List<DeprPmTableGenericRow<T_ROW_OBJ>> getGenericRows();

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
  RowSelectMode getPmRowSelectMode();

  /**
   * @return The row selection mode for this table.
   */
  RowSelectMode getRowSelectMode();

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
  T_ROW_OBJ getCurrentRowPm();

  /**
   * @return The set of all selected rows.
   */
  Collection<T_ROW_OBJ> getSelectedRows();

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
  void addDecorator(PmCommandDecorator decorator, TableChange... changes);

  /**
   * Provides the decorators to consider for a given change kind.
   *
   * @param change The change kind.
   * @return The decorators, defined for the given change kind.
   */
  Collection<PmCommandDecorator> getDecorators(TableChange change);

  /**
   * PM for table with a pager.
   *
   * @param <T_ROW_OBJ> The type used for row objects.
   */
  public static interface WithPager<T_ROW_ELEMENT> extends DeprPmTable<T_ROW_ELEMENT>{

    /**
     * @return The pager used for the table.
     */
    @Deprecated
    DeprPmPager getPager();
    
    DeprPmPager getPmPager();
  }

}
