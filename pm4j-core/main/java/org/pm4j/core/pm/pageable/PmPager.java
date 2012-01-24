package org.pm4j.core.pm.pageable;

import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmLabel;

/**
 * PM for a control that allows to switch between pages.
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 */
public interface PmPager<T_ITEM> {

  /**
   * @return A command that navigates to the first page.
   */
  PmCommand getCmdFirstPage();

  /**
   * @return A command that navigates to the previous page.
   */
  PmCommand getCmdPrevPage();

  /**
   * @return A command that navigates to the next page.
   */
  PmCommand getCmdNextPage();

  /**
   * @return A command that navigates to the last page.
   */
  PmCommand getCmdLastPage();

  /**
   * @return A label presenting a text like 'Element 5 - 10 of 54'.
   */
  PmLabel getItemXtillYofZ();

  /**
   * @return The total number of items (on all pages).
   */
  int getNumOfItems();

  /**
   * @return An attribute that can be used to jump to an entered page number.
   */
  PmAttrInteger getCurrentPageIdx();

  /**
   * Adjusts the page size.
   *
   * @param The new page size. Should be greater than zero.
   */
  void setPageSize(int pageSize);

  /**
   * @return The maximal number of items on a single page.
   */
  int getPageSize();

  /**
   * @return The total number of pages.
   */
  int getNumOfPages();

  /**
   * @return A command that allows to select all items on the current page.
   */
  PmCommand getCmdSelectAllOnPage();

  /**
   * @return A command that allows to de-select all items on the current page.
   */
  PmCommand getCmdDeSelectAllOnPage();

}