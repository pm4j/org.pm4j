package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.impl.PmPagerImpl.PagerVisibility;

/**
 * PM for a control that allows to switch between pages.
 *
 * @author olaf boede
 */
public interface PmPager {

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

  /**
   * @return A command that allows to select all items across all pages.
   */
  PmCommand getCmdSelectAll();

  /**
   * @return A command that allows to de-select all items.
   */
  PmCommand getCmdDeSelectAll();

  /**
   * @return The pager visibility rule.
   */
  PagerVisibility getPagerVisibility();

  /**
   * Registers a decorator to be called before and after a page change triggered by
   * this pager.
   * <p>
   * A decorator may prevent the page switch by returning <code>true</code> in it's
   * <code>beforDo</code> implementation.
   *
   * @param decorator The decorator.
   */
  void addPageChangeDecorator(PmCommandDecorator decorator);
}