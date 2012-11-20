package org.pm4j.core.pm;

import java.util.List;

import org.pm4j.common.query.SortOrder;
import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.FilterByDefinitionProvider;
import org.pm4j.core.util.table.ColSizeSpec;

/**
 * PM of a table column.
 *
 * @author olaf boede
 */
public interface PmTableCol extends PmObject, FilterByDefinitionProvider {

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

  /**
   * Specification and visualization of the column position is supported by
   * the PM attribute provided by this method.
   *
   * @return The position of the column within the table.
   */
  PmAttrInteger getColPosAttr();

  /**
   * Provides the set of current filter definitions that can be
   * specified for this column.
   *
   * @return The set of column filter definitions.<br>
   *         Provides an empty collection if there is no filter definition.
   *         Never <code>null</code>.
   */
  List<FilterByDefinition> getFilterByDefinitions();


}
