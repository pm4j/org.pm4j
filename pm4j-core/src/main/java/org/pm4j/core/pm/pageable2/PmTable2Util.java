package org.pm4j.core.pm.pageable2;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmTableCfg2;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.impl.PmTableUtil;
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
  public static QueryOptions makeQueryOptionsForInMemoryTable(PmTable2<?> pmTable) {
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

    for (PmTableCol col : pmTable.getColumnPms()) {
      PmTableColCfg colCfg = col.getClass().getAnnotation(PmTableColCfg.class);
      if ((colCfg.sortable() == PmBoolean.TRUE) ||
          (tableSortable && colCfg.sortable() == PmBoolean.UNDEFINED)) {
        SortOrder sortOrder = new InMemSortOrder(new RowPmComparatorAsc<PmElement>(col));
        options.addSortOrder(col.getPmName(), sortOrder);
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

// TODOs
//    for (FilterByDefinition fpd : pmTable.getFilterByDefinitions()) {
//      fpd.getName()
//      addFilterPredicateDefinition(fpd);
//    }

    return options;
  }




  public static class RowPmComparatorAsc<T_ROW_ELEMENT_PM extends PmElement> implements Comparator<T_ROW_ELEMENT_PM> {
    private final PmTableCol sortColumn;

    public RowPmComparatorAsc(PmTableCol sortColumn) {
      assert sortColumn != null;
      this.sortColumn = sortColumn;
    }

    @Override
    public int compare(T_ROW_ELEMENT_PM o1, T_ROW_ELEMENT_PM o2) {
      PmObject cellPm1 = PmTableUtil.getRowCellForTableCol(sortColumn, o1);
      PmObject cellPm2 = PmTableUtil.getRowCellForTableCol(sortColumn, o2);

      return cellPm1.compareTo(cellPm2);
    }
  }

}
