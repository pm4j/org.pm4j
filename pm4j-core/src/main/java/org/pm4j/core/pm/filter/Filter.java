package org.pm4j.core.pm.filter;

/**
 * Interface for classes used to filter collection items.
 * <p>
 * A filter may be declared to filter PM items by passing the {@link #doesItemMatch(Object)} method
 * to beans behind the PM items.<br>
 * In this case the method {@link #isBeanFilter()} should provide the value <code>true</code>.
 */
// TODO: @Deprecated - will be moved out with old PmTable
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

  /**
   * A filter may be declared to filter PM items by passing the
   * {@link #doesItemMatch(Object)} method to beans behind the PM items.<br>
   * In this case the method {@link #isBeanFilter()} should provide the value
   * <code>true</code>.
   *
   * @return <code>true</code> if the backing beans should be passed to
   *         {@link #doesItemMatch(Object)}. <code>false</code> if the PM items
   *         should be passed to {@link #doesItemMatch(Object)}.
   */
  boolean isBeanFilter();

}