package org.pm4j.common.selection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.common.util.CompareUtil;

/**
 * A selection based on a set of objects.
 * <p>
 * @param <T_ITEM> the type of selected items.
 *
 * @author Olaf Boede
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
  public boolean hasSameItemSet(Selection<T_ITEM> other) {
    if (!(other instanceof ItemSetSelection)) {
      throw new UnsupportedOperationException("Unable to compare to: " + other);
    }
    return CompareUtil.sameItemSet(selectedItems, ((ItemSetSelection<T_ITEM>)other).selectedItems);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(selectedItems);
  }

}