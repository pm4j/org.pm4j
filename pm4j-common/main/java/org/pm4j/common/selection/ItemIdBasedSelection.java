package org.pm4j.common.selection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * A selection of items that is based on a collection of item id's.
 *
 * @author olaf boede
 *
 * @param <T_ITEM> type of items to handle.
 * @param <T_ID> type of item id's.
 */
public class ItemIdBasedSelection<T_ITEM, T_ID> implements Selection<T_ITEM>, Serializable {

  /** Serialization class version. Increment on member structure change. */
  private static final long serialVersionUID = 1L;

  // TODO: preseve the sort order according to some user defined stuff
  //  For large selections an array would be more memory efficient. But it would slow down the contains() functionality...
  final Set<T_ID> ids;
  private final ItemIdConverter<T_ITEM, T_ID> itemIdConverter;

  public ItemIdBasedSelection(ItemIdConverter<T_ITEM, T_ID> itemIdConverter, Set<T_ID> ids) {
    this.ids = Collections.unmodifiableSet(ids);
    this.itemIdConverter = itemIdConverter;
  }

  @Override
  public long getSize() {
    return ids.size();
  }

  @Override
  public boolean contains(T_ITEM item) {
    return ids.contains(itemIdConverter.getIdForItem(item));
  }

  public Set<T_ID> getIds() {
    return ids;
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new ItemIdBasedIterator(ids);
  }

  class ItemIdBasedIterator implements Iterator<T_ITEM> {
    private final Iterator<T_ID> idIterator;

    public ItemIdBasedIterator(Collection<T_ID> ids) {
      this.idIterator = ids.iterator();
    }

    @Override
    public boolean hasNext() {
      return idIterator.hasNext();
    }

    @Override
    public T_ITEM next() {
      return itemIdConverter.getItemForId(idIterator.next());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}