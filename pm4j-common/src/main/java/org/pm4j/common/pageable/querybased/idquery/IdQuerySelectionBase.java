package org.pm4j.common.pageable.querybased.idquery;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.pm4j.common.pageable.querybased.NoItemForKeyFoundException;
import org.pm4j.common.pageable.querybased.QuerySelectionBase;
import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.selection.ItemIdBasedSelection;
import org.pm4j.common.selection.Selection;

/**
 * A selection of items that is based on a collection of item id's.
 *
 * @author Olaf Boede
 *
 * @param <T_ITEM> type of items to handle.
 * @param <T_ID> type of item id's.
 */
public class IdQuerySelectionBase<T_ITEM, T_ID>
    extends QuerySelectionBase<T_ITEM, T_ID>
    implements Selection<T_ITEM>, Serializable, ItemIdBasedSelection<T_ITEM, T_ID> {

  /** Serialization class version. Increment on member structure change. */
  private static final long serialVersionUID = 1L;

  // TODO: preseve the sort order according to some user defined stuff
  //  For large selections an array would be more memory efficient. But it would slow down the contains() functionality...
  private final Set<T_ID> ids;

  public IdQuerySelectionBase(QueryService<T_ITEM, T_ID> service, Set<T_ID> ids) {
    super(service);
    this.ids = Collections.unmodifiableSet(ids);
  }

  @Override
  public long getSize() {
    return ids.size();
  }

  @Override
  public boolean isEmpty() {
    return ids.isEmpty();
  }

  @Override
  public boolean contains(T_ITEM item) {
    return ids.contains(getService().getIdForItem(item));
  }

  public Collection<T_ID> getIds() {
    return ids;
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new ItemIdBasedIterator(ids);
  }

  /** Block size has no effect on this iterator implementation. */
  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
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
      T_ID id = idIterator.next();
      T_ITEM item = getService().getItemForId(id);
      if (item == null) {
        throw new NoItemForKeyFoundException("No item found for ID: " + id + ". It may have been deleted by a concurrent operation." +
        		"\n\tUsed query service: " + getService());
      }
      return item;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}