package org.pm4j.common.pageable.querybased.idquery;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.pm4j.common.pageable.querybased.NoItemForKeyFoundException;
import org.pm4j.common.pageable.querybased.QuerySelectionBase;
import org.pm4j.common.selection.ItemIdBasedSelection;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.common.util.collection.ListUtil;

/**
 * A selection of items that is based on a collection of item id's.
 *
 * @param <T_ITEM> type of items to handle.
 * @param <T_ID> type of item id's.
 *
 * @author Olaf Boede
 */
public class IdQuerySelection<T_ITEM, T_ID>
    extends QuerySelectionBase<T_ITEM, T_ID>
    implements Serializable, ItemIdBasedSelection<T_ITEM, T_ID> {

  /** Serialization class version. Increment on member structure change. */
  private static final long serialVersionUID = 1L;

  private final Set<T_ID> ids;

  private int iterationReadBlockSize = 50;

  public IdQuerySelection(IdQueryService<T_ITEM, T_ID> service, Collection<T_ID> ids) {
    super(service);
    if ( ids.size() == 0 ) {
      this.ids = Collections.emptySet();
    } else {
      this.ids = Collections.unmodifiableSet((ids instanceof Set)
          ? (Set<T_ID>)ids
          : new LinkedHashSet<T_ID>(ids));
    }
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
    return new ItemIdBasedIterator<T_ITEM, T_ID>((IdQueryService<T_ITEM, T_ID>) getService(), ids, iterationReadBlockSize);
  }

  /** Block size has no effect on this iterator implementation. */
  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    Validate.isTrue(readBlockSize > 0, "Iterator read block size must be greater than zero.");
    this.iterationReadBlockSize = readBlockSize;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean hasSameItemSet(Selection<T_ITEM> other) {
    // Compare of other selections is currently not supported.
    if (!(other instanceof IdQuerySelection)) {
    	throw new UnsupportedOperationException("Unable to compare to: " + other) ;
    }
    return CompareUtil.equalItemSet(ids, ((IdQuerySelection<T_ITEM, T_ID>)other).ids);
  }

  @Override
  public String toString() {
    return ids.toString();
  }

  /**
   * Iterates block-wise over a set if IDs. Uses the method {@link IdQueryService#getItems(List)}
   * to read the items to iterate.
   *
   * @param <T_ITEM> The type of items to provide.
   * @param <T_ID> The item ID type.
   */
  static class ItemIdBasedIterator<T_ITEM, T_ID> implements Iterator<T_ITEM> {
    private final IdQueryService<T_ITEM, T_ID> service;
    private final int readBlockSize;

    private List<T_ITEM> readBlock = Collections.emptyList();
    private int readBlockIdx;

    private List<T_ID> ids;
    private int idIdx = 0;

    /**
     * @param service Used to retrieve items for a set of given IDs.
     * @param ids The IDs of the items to iterate.
     * @param iterationReadBlockSize The read chunk size.
     */
    public ItemIdBasedIterator(IdQueryService<T_ITEM, T_ID> service, Collection<T_ID> ids, int iterationReadBlockSize) {
      this.service = service;
      this.ids = ListUtil.toList(ids);
      this.readBlockSize = iterationReadBlockSize;
      this.readBlockIdx = readBlockSize;
    }

    @Override
    public boolean hasNext() {
      return idIdx < ids.size();
    }

    @Override
    public T_ITEM next() {
      if (readBlockIdx >= readBlockSize) {
        int endIdx = Math.min(idIdx + readBlockSize, ids.size());
        List<T_ID> blockIds = ids.subList(idIdx, endIdx);
        List<T_ITEM> blockItems = service.getItems(blockIds);
        readBlock = IdQueryServiceUtil.sortByIdListWithGaps(service, blockItems, blockIds);
        readBlockIdx = 0;
      }
      ++idIdx;
      T_ITEM item = readBlock.get(readBlockIdx++);
      if (item == null) {
        throw new NoItemForKeyFoundException(ids.get(idIdx-1), service);
      }
      return item;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

}