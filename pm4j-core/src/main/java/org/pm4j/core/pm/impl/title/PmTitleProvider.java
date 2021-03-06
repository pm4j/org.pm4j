package org.pm4j.core.pm.impl.title;





/**
 * Interface for classes that implement some algorithms to provide
 * internationalized title resources.
 *
 * @param T The type of the titled resource.
 *
 * @author olaf boede
 */
public interface PmTitleProvider<T> {

  /**
   * @param item
   *          An item to get a title for.
   * @return The title string for the given item.
   */
  String getTitle(T item);

  /**
   * @see PmTitledObject#getPmShortTitle()
   * @param item
   *          An item to get the short title for.
   * @return The short title string for the given item or <code>null</code> if
   *         there is no special short title configured.
   */
  String getShortTitle(T item);

  /**
   * @param item
   *          The item to get a tooltip for.
   * @return A tooltip string or <code>null</code> when there is no tooltip
   *         for the given item.
   */
  String getToolTip(T item);

  /**
   * @param item
   *          The item to get a icon name for.
   * @return A icon string or <code>null</code> when there is no icon
   *         for the given item.
   */
  String getIconPath(T item);

}
