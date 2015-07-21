package org.pm4j.common.selection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.pm4j.common.util.CompareUtil;
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
    Validate.notNull(baseSelection);

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
  public boolean contains(T_ITEM item) {
    return baseSelection.contains(item) || additionalSelectedItems.contains(item);
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new CombinedIterator<T_ITEM>(baseSelection.iterator(), additionalSelectedItems.iterator());
  }

  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    baseSelection.setIteratorBlockSizeHint(readBlockSize);
  }

  /**
   * @return The set of additional selected items.<br>
   *         An empty collection of no additional item is selected.
   */
  public List<T_ITEM> getAdditionalSelectedItems() {
    return additionalSelectedItems;
  }

  public Selection<T_ITEM> getBaseSelection() {
    return baseSelection;
  }

  @Override
  public boolean hasSameItemSet(Selection<T_ITEM> other) {
    // Compare of other selections is currently not supported.
    if (!(other instanceof SelectionWithAdditionalItems)) {
      throw new UnsupportedOperationException("Unable to compare to: " + other);
    }
    SelectionWithAdditionalItems<T_ITEM> otherSelection = (SelectionWithAdditionalItems<T_ITEM>) other;

    return CompareUtil.sameItemSet(additionalSelectedItems, otherSelection.additionalSelectedItems) &&
           SelectionHandlerUtil.sameSelection(baseSelection, otherSelection.baseSelection);
  }


}
