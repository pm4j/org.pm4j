package org.pm4j.core.pm.impl;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmPager.PagerVisibility;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.impl.pageable.PmBeanCollection;

/**
 * Some table related helper functions.
 *
 * @author olaf boede
 */
public final class PmTableUtil {

  private static Logger LOG = Logger.getLogger(PmTableUtil.class);

  /**
   * Creates a {@link PageableCollection} that uses the given collection and assigns it to the given {@link PmTable}.
   * <p>
   * In difference to overriding {@link PmTableImpl#getPmBeansImpl()}, this is a fix assignment. It will only change
   * the collection reference this method gets called again.
   *
   * @param tablePm The table that should present the given collection.
   * @param beans The collection. May be <code>null</code>.
   */
  public static <T_ROW_PM extends PmBean<T_ROW_BEAN>, T_ROW_BEAN> void setPmBeans(PmTableImpl<T_ROW_PM, T_ROW_BEAN> tablePm, Collection<T_ROW_BEAN> beans) {
    if (tablePm.pmCollectionGetterLogicUsed) {
      // TODO oboede:
      LOG.error("The table uses getter logic. A mix of getter and setter logic is not supported.\n" +
                "Please use consistently only getter or only setter logic for a table instance.",
                new RuntimeException());
//      throw new PmRuntimeException(tablePm, "The table uses getter logic. A mix of getter and setter logic is not supported.\n" +
//                                            "Please use consistently only getter or only setter logic for a table instance.");
    }

    Class<T_ROW_PM> beanClass = tablePm.getPmRowBeanClass();
    QueryOptions qo = tablePm.getPmQueryOptions();
    // The query parameters are part of the pageable collection.
    // Make sure that predefined query parameter values don't disappear.
    // That may happen if the table did set some fix conditions on pmInit.
    QueryParams qp = tablePm.getPmQueryParams();
    tablePm.setPmPageableCollection(new PmBeanCollection<T_ROW_PM, T_ROW_BEAN>(tablePm, beanClass, beans, qo));
    tablePm.getPmQueryParams().copyParamValues(qp);
  }

  /**
   * Provides the index of the given row object within it's table.
   *
   * @param rowPm The PM of the row to check.
   * @return The index (first item has the index zero).<br>
   *    <code>-1</code> if the given row is not on the current page.
   */
  public static int findIndexOfRowOnCurrentPage(PmElement rowPm) {
    PmTable<?> tablePm = PmUtil.getPmParentOfType(rowPm, PmTable.class);
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
  public static void setScrollableTableProperties(PmTableImpl<?, ?> table) {
    table.setNumOfPageRowPms(Integer.MAX_VALUE);
    if (table.getPmPager() != null) {
      table.getPmPager().setPagerVisibility(PagerVisibility.NEVER);
    }
  }

}
