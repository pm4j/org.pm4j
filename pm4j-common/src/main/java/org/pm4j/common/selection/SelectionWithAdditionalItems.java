package org.pm4j.common.selection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.util.collection.CombinedIterator;
import org.pm4j.common.util.collection.ListUtil;


/**
 * A selection that uses a base selection as well as a set of additional selected items.
 *
 * @param <T_ITEM> item type.
 */
public class SelectionWithAdditionalItems <T_ITEM> implements Selection<T_ITEM> {
  private static final long       serialVersionUID = 1L;
  private final Selection<T_ITEM> baseSelection;
  private final List<T_ITEM>      additionalSelectedItems;

  @SuppressWarnings("unchecked")
  public SelectionWithAdditionalItems(Selection<T_ITEM> baseSelection, Collection<T_ITEM> selectedTransientItems) {
    assert baseSelection != null;

    this.baseSelection = baseSelection;
    this.additionalSelectedItems = (selectedTransientItems != null && !selectedTransientItems.isEmpty())
        ? Collections.unmodifiableList(ListUtil.toList(selectedTransientItems))
        : Collections.EMPTY_LIST;
  }

  @Override
  public long getSize() {
    return baseSelection.getSize() + additionalSelectedItems.size();
  }

  @Override
  public boolean isEmpty() {
    return getSize() == 0L;
  }

  @Override
  public boolean isSelected(T_ITEM item) {
    return baseSelection.isSelected(item) || additionalSelectedItems.contains(item);
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new CombinedIterator<T_ITEM>(baseSelection.iterator(), additionalSelectedItems.iterator());
  }

  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    baseSelection.setIteratorBlockSizeHint(readBlockSize);
  }

  public List<T_ITEM> getAdditionalSelectedItems() {
    return additionalSelectedItems;
  }

  public Selection<T_ITEM> getBaseSelection() {
    return baseSelection;
  }
}
