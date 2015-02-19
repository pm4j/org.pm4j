package org.pm4j.core.pm;

import org.pm4j.common.pageable.PageableCollection;

/**
 * PM for a control that allows to switch between pages.
 *
 * @author olaf boede
 */
public interface PmPager {

  /**
   * The set of standard pager visibility conditions.
   */
  public enum PagerVisibility {
    /** The pager will always be displayed. */
    ALWAYS,
    /** The pager will be displayed only if there is at least a second page to navigate to. */
    WHEN_SECOND_PAGE_EXISTS,
    /** The pager will be displayed only if the table has at least a single row. */
    WHEN_TABLE_IS_NOT_EMPTY,
    /** The pager will not be displayed. */
    NEVER
  }


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
  PmObject getItemXtillYofZ();

  /**
   * @return The total number of items (on all pages).
   */
  int getNumOfItems();

  /**
   * @return An attribute that can be used to jump to an entered page number.
   */
  PmAttrLong getCurrentPageIdx();

  /**
   * @return The maximal number of items on a single page.
   */
  int getPageSize();

  /**
   * @return The total number of pages.
   */
  long getNumOfPages();

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
   * @param pagerVisibility The pager visibility rule to use.
   */
  void setPagerVisibility(PagerVisibility pagerVisibility);


  /**
   * @param pageableCollection The pageable collection to handle.
   */
  void setPageableCollection(PageableCollection<?> pageableCollection);

  /**
   * Registers a decorator to be called before and after a page change triggered by
   * this pager.
   * <p>
   * A decorator may prevent the page switch by returning <code>false</code> in it's
   * <code>beforDo</code> implementation.
   *
   * @param decorator The decorator.
   */
  void addPageChangeDecorator(PmCommandDecorator decorator);
}