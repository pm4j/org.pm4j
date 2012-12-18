package org.pm4j.core.pm.pageable2;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.FilterCompareDefinitionFactory;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol2;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmTableCfg2;
import org.pm4j.core.pm.annotation.PmTableColCfg2;
import org.pm4j.core.pm.impl.AnnotationUtil;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmTableColImpl2;
import org.pm4j.core.pm.impl.PmTableImpl2;
import org.pm4j.core.util.reflection.ClassUtil;

/**
 * Reads the query options from the specifications done for the table columns.
 *
 * @author olaf boede
 */
public class PmTable2Util {

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
          SortOrder sortOrder = new InMemSortOrder(new RowPmComparatorAsc<PmElement>(col));
          options.addSortOrder(col.getPmName(), sortOrder);
        }

        if (colCfg.filterType() != Void.class) {
          // The default logic: the filter compares the bean field with the same name.
          // TODO olaf: make it configurable within the column configuration.
          AttrDefinition a = new AttrDefinition(col.getPmName(), "pmBean." + col.getPmName(), colCfg.filterType());
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
   * @param rowElement
   *          PM of the row that contains the cell.
   * @return The found cell PM.
   * @throws PmRuntimeException
   *           if there is no corresponding cell.
   */
  public static PmObject getRowCellForTableCol(PmTableCol2 column, PmElement rowElement) {
    PmObject pm = ((PmTableColImpl2) column).findCorrespondingRowCell(rowElement);
    if (pm == null) {
      throw new PmRuntimeException(column, "No corresponding table row cell PM found in row: " + rowElement);
    }
    return pm;
  }



  public static class RowPmComparatorAsc<T_ROW_ELEMENT_PM extends PmElement> implements Comparator<T_ROW_ELEMENT_PM> {
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

}
