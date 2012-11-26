package org.pm4j.common.pageable.querybased;

import java.util.List;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.query.QueryParams;

/**
 * A page cache for item provided by a {@link PageableCollection2}.
 * <p>
 * TODO olaf: does not yet observe queryParam changes to release cached data.
 *
 * @author olaf boede
 */
public class PageableQueryCollectionCache<T> {
  private List<T> cachedPage;
  private long pageStartPos = -1;
  private long pageEndPos = -1;
  private int pageSize;
  private long queryResultSetSize = -1;
  private final PageableQueryService<T, ?> pageableQueryService;
  private final QueryParams queryParams;

  public PageableQueryCollectionCache(PageableQueryService<T, ?> pageableQueryService, QueryParams queryParams,
      int pageSize) {
    assert pageableQueryService != null;
    assert queryParams != null;
    assert pageSize > 0;

    this.pageableQueryService = pageableQueryService;
    this.queryParams = queryParams != null ? queryParams : new QueryParams();
    this.pageSize = pageSize;

  }

  /**
   * Clears all cached data.
   */
  public void clear() {
    cachedPage = null;
    queryResultSetSize = -1;
    pageEndPos = -1;
    pageStartPos = -1;
  }

  public T getAt(long pos) {
    if (cachedPage == null || pos < pageStartPos || pos > pageEndPos) {
      pageStartPos = Math.min(pos - (pos % pageSize), getQueryResultSetSize());
      cachedPage = pageableQueryService.getItems(queryParams, pageStartPos, pageSize);
      pageEndPos = pageStartPos + cachedPage.size() - 1;
    }
    return cachedPage.get(posToCachePos(pos));
  }

  public long getQueryResultSetSize() {
    if (queryResultSetSize == -1) {
      queryResultSetSize = pageableQueryService.getItemCount(queryParams);
    }
    return queryResultSetSize;
  }

  private int posToCachePos(long pos) {
    return (int) (pos - pageStartPos);
  }
}
