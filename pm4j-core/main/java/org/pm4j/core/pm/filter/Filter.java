package org.pm4j.core.pm.filter;

/**
 * Interface for classes used to filter collection items.
 */
public interface Filter {

  /**
   * Checks if the given item matches.
   *
   * @param item
   *          The item to check.
   * @return <code>true</code> if the item should be visible.<br>
   *         <code>false</code> if the item should be hidden.
   */
  boolean doesItemMatch(Object item);

}