package org.pm4j.common.util.collection;

import java.util.List;

/**
 * A paged cache for a virtual collection of items.
 * <p>
 * The implementation of {@link #getPageItems(long, long)} and {@link #getTotalSize()}
 * provide access to the virtual collection items.
 *
 * @author olaf boede
 */
public abstract class PagedCollectionCacheBase<T> {
  private List<T>   cachedPage;
  private long      pageStartPos    = -1;
  private long      pageEndPos      = -1;
  private int       pageSize;
  private long      collectionSize  = -1;

  public PagedCollectionCacheBase(int pageSize) {
    assert pageSize > 0;
    this.pageSize = pageSize;
  }

  /**
   * Clears all cached data.
   */
  public void clear() {
    cachedPage = null;
    collectionSize = -1;
    pageStartPos = -1;
    pageEndPos = -1;
  }

  /**
   * Allows to re-adjust the used cache page size.
   *
   * @param pageSize the new page size. Should be greater than zero.
   */
  public void setPageSize(int pageSize) {
    assert pageSize > 0;
    this.pageSize = pageSize;
    clear();
  }

  protected abstract List<T> getPageItems(long pageStartPos, int pageSize);
  protected abstract long getTotalSize();

  public T getAt(long pos) {
    if (cachedPage == null || pos < pageStartPos || pos > pageEndPos) {
      pageStartPos = Math.min(pos - (pos % pageSize), getCollectionSize());
      cachedPage = getPageItems(pageStartPos, pageSize);
      pageEndPos = pageStartPos + cachedPage.size() - 1;
    }
    return cachedPage.get(posToCachePos(pos));
  }

  public long getCollectionSize() {
    if (collectionSize == -1) {
      collectionSize = getTotalSize();
    }
    return collectionSize;
  }

  private int posToCachePos(long pos) {
    return (int) (pos - pageStartPos);
  }
}
