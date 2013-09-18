package org.pm4j.core.pm.impl;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.FilterCompareDefinitionFactory;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmPager2.PagerVisibility;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol2;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmTableCfg2;
import org.pm4j.core.pm.annotation.PmTableColCfg2;
import org.pm4j.core.pm.impl.changehandler.MasterPmHandler;
import org.pm4j.core.util.reflection.ClassUtil;

/**
 * Some table related helper functions.
 *
 * @author olaf boede
 */
public final class PmTableUtil2 {

  public static class RowPmComparatorAsc<T_ROW_ELEMENT_PM extends PmObject> implements Comparator<T_ROW_ELEMENT_PM> {
    private final PmTableCol2 sortColumn;

    public RowPmComparatorAsc(PmTableCol2 sortColumn) {
      assert sortColumn != null;
      this.sortColumn = sortColumn;
    }

    @Override
    public int compare(T_ROW_ELEMENT_PM o1, T_ROW_ELEMENT_PM o2) {
      PmObject cellPm1 = getRowCellForTableCol(sortColumn, o1);
      PmObject cellPm2 = getRowCellForTableCol(sortColumn, o2);

      return cellPm1.compareTo(cellPm2);
    }
  }

  /**
   * Provides the index of the given row object within it's table.
   *
   * @param rowPm The PM of the row to check.
   * @return The index (first item has the index zero).<br>
   *    <code>-1</code> if the given row is not on the current page.
   */
  public static int findIndexOfRowOnCurrentPage(PmElement rowPm) {
    PmTable2<?> tablePm = PmUtil.getPmParentOfType(rowPm, PmTable2.class);
    List<?> rows = tablePm.getRowPms();
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
    table.setNumOfPageRowPms(Integer.MAX_VALUE);
    if (table.getPmPager() != null) {
      table.getPmPager().setPagerVisibility(PagerVisibility.WHEN_SECOND_PAGE_EXISTS);
    }
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
    handler.startObservers();
    return handler;
  }

  /**
   * @param pmTable the table to get the query options for.
   */
  public static QueryOptions makeQueryOptionsForInMemoryTable(PmTableImpl2<?, ?> pmTable) {
    assert pmTable != null;
    QueryOptions options = new QueryOptions();

    PmTableCfg2 tblCfg = pmTable.getClass().getAnnotation(PmTableCfg2.class);
    boolean tableSortable = false;
    Class<?> initialBeanSortComparatorClass = null;
    String defaultSortColName = null;

    if (tblCfg != null) {
      tableSortable = tblCfg.sortable();
      initialBeanSortComparatorClass = tblCfg.initialBeanSortComparator();
      defaultSortColName = tblCfg.defaultSortCol();
    }

    FilterCompareDefinitionFactory f = pmTable.getPmFilterCompareDefinitionFactory();
    for (PmTableCol2 col : pmTable.getColumnPms()) {
      PmTableColCfg2 colCfg = AnnotationUtil.findAnnotation((PmObjectBase)col, PmTableColCfg2.class);
      if (colCfg != null) {
        if ((colCfg.sortable() == PmBoolean.TRUE) ||
           (tableSortable && colCfg.sortable() == PmBoolean.UNDEFINED)) {
          SortOrder sortOrder = new InMemSortOrder(new PmTableUtil2.RowPmComparatorAsc<PmElement>(col));
          options.addSortOrder(col.getPmName(), sortOrder);
        }

        if (colCfg.filterType() != Void.class) {
          // The default logic: the filter compares the bean field with the same name.
          // TODO olaf: make it configurable within the column configuration.
          QueryAttr a = new QueryAttr(col.getPmName(), "pmBean." + col.getPmName(), colCfg.filterType());
          options.addFilterCompareDefinition(f.createCompareDefinition(a));
        }
      }
    }

    if (defaultSortColName != null) {
      String name = StringUtils.substringBefore(defaultSortColName, ",");
      SortOrder so = options.getSortOrder(name);
      if (so == null) {
        throw new PmRuntimeException(pmTable, "default sort column '" + defaultSortColName + "' is not a sortable column.");
      }
      if ("desc".equals(StringUtils.trim(StringUtils.substringAfter(defaultSortColName, ",")))) {
        so = so.getReverseSortOrder();
      }
      options.setDefaultSortOrder(so);
    }

    if (initialBeanSortComparatorClass != null) {
      if (options.getDefaultSortOrder() != null) {
        throw new PmRuntimeException(pmTable, "Duplicate default sort order definition found in PmTableCfg annotation.");
      }

      Comparator<Object> c = ClassUtil.newInstance(initialBeanSortComparatorClass);
      options.setDefaultSortOrder(new InMemSortOrder(c));
    }

    return options;
  }

  /**
   * Provides the corresponding cell PM for a column within the given row.
   * <p>
   * The implementation of <code>PmTableColImpl#findCorrespondingRowCell</code>
   *
   * @param column
   *          The column to find a row cell for.
   * @param rowPm
   *          PM of the row that contains the cell.
   * @return The found cell PM.
   * @throws PmRuntimeException
   *           if there is no corresponding cell.
   */
  public static PmObject getRowCellForTableCol(PmTableCol2 column, PmObject rowPm) {
    PmObject pm = ((PmTableColImpl2) column).findCorrespondingRowCell(rowPm);
    if (pm == null) {
      throw new PmRuntimeException(column, "No corresponding table row cell PM found in row: " + rowPm);
    }
    return pm;
  }

}
