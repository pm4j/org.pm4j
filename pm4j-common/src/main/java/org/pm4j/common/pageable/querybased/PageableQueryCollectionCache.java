package org.pm4j.common.pageable.querybased;

import java.util.List;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.util.collection.PagedCollectionCacheBase;

/**
 * A page cache for items provided by a {@link PageableCollection2}.
 * <p>
 * TODO olaf: does not yet observe queryParam changes to release cached data.
 *
 * @author olaf boede
 */
public class PageableQueryCollectionCache<T> extends PagedCollectionCacheBase<T> {
  private final PageableQueryService<T, ?> pageableQueryService;
  private final QueryParams queryParams;

  public PageableQueryCollectionCache(PageableQueryService<T, ?> pageableQueryService, QueryParams queryParams,
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
