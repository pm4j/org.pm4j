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
   * @return <code>true</code> if no item is selected.
   */
  boolean isEmpty();

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
   * Please consider this iterater may have some performance impact in case of
   * lazy load scenarios.
   * <p>
   * Please check if you can use {@link #contains(Object)}, which is much
   * faster in most cases.
   */
  @Override
  Iterator<T_ITEM> iterator();

  /**
   * Provides a hint for block wise optimized iterator read operation.
   * <p>
   * Some service based iterator implementations may use the block size hint to prevent
   * too frequent query executions.
   * <p>
   * The block size parameter will have no effect for other iterators.
   *
   * @param readBlockSize a hint for the optimal query block size.
   */
  void setIteratorBlockSizeHint(int readBlockSize);

}
