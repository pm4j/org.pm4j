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
   *          An item to get the undecorated title for.
   * @return The undecorated title string for the given item.
   * @deprecated Please use getPmTitle() instead.
   */
  @Deprecated String getShortTitle(T item);

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

  /**
   * Indicates if it is allowed to call the
   * {@link #setTitle(Object, Object, String)} method for the given item.
   *
   * @param item
   * @return <code>true</code> when it is allowed to call
   *         {@link #setTitle(Object, Object, String)}.
   */
  boolean canSetTitle(T item);

  /**
   * An interface for 'in place editing' of node titles.
   * <p>
   * That might be a useful feature for title string editors of tree views where
   * a user can simply click on a title and change it.
   *
   * @param item
   * @param titleString
   */
  void setTitle(T item, String titleString);

}
