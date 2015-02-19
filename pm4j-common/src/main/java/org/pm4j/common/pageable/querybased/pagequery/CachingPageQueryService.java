package org.pm4j.common.pageable.querybased.pagequery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.util.beanproperty.ReleaseOnPropChangeRef;

/**
 * A proxy service that provides some caching for queries that are already answered.
 * If subsequent queries for one and the same query are requested, always the result for the first
 * call gets returned.
 * <p>
 * Observes the provided {@link QueryParams}. It resets cached data if the query parameter data
 * get changed.
 *
 * @author olaf boede
 *
 * @param <T_ITEM> type of collection items.
 * @param <T_ID> type of collection item id's.
 */
class CachingPageQueryService<T_ITEM, T_ID> implements PageQueryService<T_ITEM, T_ID> {
  private static final Logger LOG = LoggerFactory.getLogger(CachingPageQueryService.class);

  private final PageQueryService<T_ITEM, T_ID> baseService;
  private final CachingPageQueryService.Cache<T_ITEM, T_ID> cache;

  /**
   * Creates a caching proxy service for the given base service.
   *
   * @param service the service to cache data for.
   */
  public CachingPageQueryService(PageQueryService<T_ITEM, T_ID> service) {
    assert service != null;
    this.baseService = service;
    this.cache = new CachingPageQueryService.Cache<T_ITEM, T_ID>(service);
  }

  @Override
  public T_ID getIdForItem(T_ITEM item) {
    return baseService.getIdForItem(item);
  }

  @Override
  public T_ITEM getItemForId(T_ID id) {
    T_ITEM i = cache.idToPageItemsCache.get(id);
    return (i != null)
            ? i
            : baseService.getItemForId(id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_ITEM> getItems(QueryParams query, long startIdx, int pageSize) {
    if (!query.isExecQuery()) {
      return Collections.EMPTY_LIST;
    }

    if ((query == cache.pageCacheQuery.getRef()) &&
      (startIdx == cache.cachedPageStartIdx) &&
        (pageSize == cache.cachedPageSize)
        ) {
      return cache.pageItemsCache;
    }
    else {
      List<T_ITEM> items = baseService.getItems(query, startIdx, pageSize);
      cache.setPageCache(query, items, startIdx, pageSize);
      return items;
    }
  }

  @Override
  public long getItemCount(QueryParams query) {
    if ((query != cache.itemCountCacheQuery.getRef()) ||
        (cache.itemCountCache == -1)) {
      cache.itemCountCacheQuery.setRefQuietly(query);
      cache.itemCountCache = baseService.getItemCount(query);
    }
    return cache.itemCountCache;
  }

  /**
   * Provides access to the cache. E.g. for directly re-setting the cache.
   *
   * @return the cache. Never <code>null</code>.
   */
  public Cache<T_ITEM, T_ID> getCache() {
    return cache;
  }

  /**
   * The service behind this caching proxy.
   *
   * @return the backing service.
   */
  public PageQueryService<T_ITEM, T_ID> getBaseService() {
    return baseService;
  }

  static class Cache<T_ITEM, T_ID> {
    private final PageQueryService<T_ITEM, T_ID> service;
    private long                                     cachedPageStartIdx = -1;
    private int                                      cachedPageSize = -1;
    private List<T_ITEM>                             pageItemsCache;
    private Map<T_ID, T_ITEM>                        idToPageItemsCache = Collections.emptyMap();
    private final ReleaseOnPropChangeRef<QueryParams> pageCacheQuery;
    private final ReleaseOnPropChangeRef<QueryParams> itemCountCacheQuery;
    /** Cached number of items for the current {@link #itemCountCacheQuery}. */
    private long                                     itemCountCache = -1;

    public Cache(PageQueryService<T_ITEM, T_ID> service) {
      assert service != null;
      this.service = service;
      this.pageCacheQuery = new ReleaseOnPropChangeRef<QueryParams>(null, QueryParams.PROP_EFFECTIVE_FILTER, QueryParams.PROP_EFFECTIVE_SORT_ORDER) {
        @Override
        protected void onSetRef() {
          clearPageCache();
        }
      };
      this.itemCountCacheQuery = new ReleaseOnPropChangeRef<QueryParams>(null, QueryParams.PROP_EFFECTIVE_FILTER) {
        @Override
        protected void onSetRef() {
          clearItemCountCache();
        }
      };
    }

    public void clearPageCache() {
      if ((pageItemsCache != null) && LOG.isTraceEnabled()) {
        LOG.trace("Clearing page cache for query service: " + service.getClass().getSimpleName());
      }

      pageCacheQuery.setRefQuietly(null);
      cachedPageStartIdx = -1;
      cachedPageSize = -1;
      idToPageItemsCache = Collections.emptyMap();
      pageItemsCache = null;
    }

    public void clearItemCountCache() {
      if ((itemCountCache != -1) && LOG.isTraceEnabled()) {
        LOG.trace("Clearing item count cache for query service: " + service.getClass().getSimpleName());
      }

      itemCountCacheQuery.setRefQuietly(null);
      itemCountCache = -1;
    }

    public void clear() {
      clearPageCache();
      clearItemCountCache();
    }

    public void setPageCache(QueryParams forQuery, List<T_ITEM> pageItemsCache, long startIdx, int pageSize) {
      HashMap<T_ID, T_ITEM> id2Items = new HashMap<T_ID, T_ITEM>();
      for (T_ITEM i : pageItemsCache) {
        T_ID id = service.getIdForItem(i);
        id2Items.put(id, i);
      }
      this.pageCacheQuery.setRefQuietly(forQuery);
      this.cachedPageStartIdx = startIdx;
      this.cachedPageSize = pageSize;
      this.pageItemsCache = pageItemsCache;
      this.idToPageItemsCache = id2Items;
    }
  }
}
