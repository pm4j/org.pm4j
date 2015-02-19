package org.pm4j.common.pageable.querybased.pagequery;

import java.util.List;

import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.util.collection.PagedCollectionCacheBase;

/**
 * A page cache for items provided by a {@link PageableCollection}.
 * <p>
 * TODO olaf: does not yet observe queryParam changes to release cached data.
 *
 * @author olaf boede
 */
public class PageQueryCollectionCache<T> extends PagedCollectionCacheBase<T> {
  private final PageQueryService<T, ?> pageableQueryService;
  private final QueryParams queryParams;

  public PageQueryCollectionCache(PageQueryService<T, ?> pageableQueryService, QueryParams queryParams,
      int pageSize) {
    super(pageSize);

    assert pageableQueryService != null;
    assert queryParams != null;

    this.pageableQueryService = pageableQueryService;
    this.queryParams = queryParams != null ? queryParams : new QueryParams();
  }

  @Override
  protected List<T> getPageItems(long pageStartPos, int pageSize) {
    return pageableQueryService.getItems(queryParams, pageStartPos, pageSize);
  }

  @Override
  protected long getTotalSize() {
    return pageableQueryService.getItemCount(queryParams);
  }
}
