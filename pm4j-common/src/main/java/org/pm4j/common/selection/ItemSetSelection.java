package org.pm4j.common.selection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A selection based on a set of objects.
 * <p>
 * TODO olaf: missing sort order
 *
 * @param <T_ITEM> the type of selected items.
 *
 * @author olaf boede
 */
public class ItemSetSelection<T_ITEM> implements Selection<T_ITEM> {
  private static final long serialVersionUID = 1L;

  final Set<T_ITEM> selectedItems;

  @SuppressWarnings("unchecked")
  public ItemSetSelection(Set<T_ITEM> selectedItems) {
    this.selectedItems = (selectedItems.isEmpty())
        ? Collections.EMPTY_SET
        : Collections.unmodifiableSet(selectedItems);
  }

  public ItemSetSelection(T_ITEM... items) {
    this(new HashSet<T_ITEM>(Arrays.asList(items)));
  }

  @Override
  public long getSize() {
    return selectedItems.size();
  }

  @Override
  public boolean isEmpty() {
    return selectedItems.isEmpty();
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return selectedItems.iterator();
  }

  /** Block size has no effect on this iterator implementation. */
  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
  }

  @Override
  public boolean contains(T_ITEM item) {
    return selectedItems.contains(item);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + selectedItems;
  }

}