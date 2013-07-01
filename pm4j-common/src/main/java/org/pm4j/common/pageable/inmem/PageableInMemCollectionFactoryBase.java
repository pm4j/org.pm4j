package org.pm4j.common.pageable.inmem;

import java.util.Collection;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionFactory;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;

/**
 * Base class for in-memory {@link PageableCollectionFactory}s.
 * <p>
 * Concrete classes only need to implement {@link #getBackingCollectionImpl()} to
 * provide the data to be shown.
 *
 * @param <T_ITEM> type of collection items.
 *
 * @author olaf boede
 * @deprecated Please use {@link PmTableCfg2#valuePath()} or override <code>PmTableImpl2#getPmBeans()</code> instead.
 */
public abstract class PageableInMemCollectionFactoryBase<T_ITEM> implements PageableCollectionFactory<T_ITEM> {

  @Override
  public PageableCollection2<T_ITEM> create(QueryOptions queryOptions, QueryParams queryParams) {
    return new PageableInMemCollectionBase<T_ITEM>(queryOptions, queryParams) {
      @Override
      protected Collection<T_ITEM> getBackingCollectionImpl() {
        return PageableInMemCollectionFactoryBase.this.getBackingCollectionImpl();
      }
    };
  }

  /**s
   * Provides the backing collection.
   *
   * @return the collection. May be <code>null</code>.
   */
  protected abstract Collection<T_ITEM> getBackingCollectionImpl();

}