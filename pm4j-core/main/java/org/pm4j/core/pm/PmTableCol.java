package org.pm4j.core.pm;

import java.util.Comparator;
import java.util.List;

import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.FilterByDefinitionProvider;
import org.pm4j.core.pm.pageable.PageableCollection;
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


  /**
   * Provides the configured row sort comparator. It no specific comparator is configured, the default
   * implementation simply compares the column specific cell-PMs.
   * <p>
   * The type of items to compare depends on the kind of {@link PageableCollection} container behind the table.
   *
   * @return The configured comparator or <code>null</code>.
   */
  // XXX olaf: move to Impl?
  Comparator<?> getRowSortComparator();

}
