package org.pm4j.common.selection;

import java.io.Serializable;
import java.util.Iterator;

/**
 * A set of selected items.
 * <p>
 * Is used/provided by {@link SelectionHandler}s and selection events to be able to report
 * the old and the new set of selected items.
 *
 * @param <T_ITEM> the handled item type.
 *
 * @author olaf boede
 */
public interface Selection<T_ITEM> extends Iterable<T_ITEM>, Serializable {

  /**
   * Provides the number of selected items.
   *
   * @return The selected item count.
   */
  long getSize();

  /**
   * Checks if the item is selected.
   *
   * @param item the item to check.
   * @return <code>true</code> if the item is selected.
   */
  boolean contains(T_ITEM item);

  /**
   * The set of items can be accessed through an {@link Iterator}.
   * <p>
   * Please consider this iterater may have some performance impact in case
   * of lazy load scenarios.
   * <p>
   * Please check if you can use {@link #contains(Object)}, which is much faster.
   */
  @Override
  Iterator<T_ITEM> iterator();

  /**
   * In case of a stacked selection implementation a {@link Selection} of PMs
   * is usually backed by a {@link Selection} of beans.
   * <p>
   * This method provides access to this bean selection.<br>
   * The result of this call may provide this instance or one backing selection.
   *
   * @return the bean selection.
   */
  <T_BEAN> Selection<T_BEAN> getBeanSelection();
}
