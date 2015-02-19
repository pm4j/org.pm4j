package org.pm4j.core.pm;

import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.query.filter.FilterDefinitionFactory;
import org.pm4j.core.util.table.ColSizeSpec;

/**
 * PM of a table column.
 *
 * @author olaf boede
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

  // --- PM INTERAL interface ---

  /**
   * PLEASE DON'T USE THIS METHOD IN BUSINESS CODE!
   *
   * @return The details needed for other PMs (e.g. the table implementation).
   */
  ImplDetails getPmImplDetails();

  /**
   * An INTERNAL implementation interface that is used to provide column details to other tight coupled
   * PMs.
   * <p>
   * Please don't use it in business code!
   */
  interface ImplDetails {

    /**
     * Provides the name used to find the corresponding {@link QueryAttr} for sorting and filtering this column.
     * <p>
     * In case of in-memory tables it is also used for the default query attribute creation mechanism.
     * <p>
     * By default it provides the result of <code>getPmName()</code> but in some special cases (e.g. generic tables)
     * this may be different.
     *
     * @return The query attribute name.
     */
    String getQueryAttrName();

    /**
     * @return The column {@link QueryAttr} that may be used for sorting and filtering. May be <code>null</code>.
     */
    QueryAttr getQueryAttr();

    /**
     * @return The supported column sort order. May be defined by annotation or by implementation.
     */
    Boolean isSortableConfigured();

    /**
     * Filterable columns provide here their {@link FilterDefinition} that may be
     * used to offer some user filter functionality.
     *
     * @return The compare definition or <code>null</code> if the column is not filterable.
     */
    FilterDefinition getFilterCompareDefinition(FilterDefinitionFactory fcdf);

  }

}
