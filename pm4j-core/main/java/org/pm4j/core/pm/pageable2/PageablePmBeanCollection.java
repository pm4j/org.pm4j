package org.pm4j.core.pm.pageable2;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.inmem.PageableInMemCollectionImpl;
import org.pm4j.common.pageable.querybased.PageableQueryCollection;
import org.pm4j.common.pageable.querybased.PageableQueryService;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.pageable.PageableCollection;
import org.pm4j.core.pm.pageable.PageableListImpl;

/**
 * A {@link PageableCollection2} instance that provides {@link PmBean} instances in
 * front of a {@link PageableCollection2} container that handles the corresponding
 * bean instances.
 *
 * @author olaf boede
 *
 * @param <T_PM>
 *          The kind of {@link PmBean} provided by this class.
 * @param <T_BEAN>
 *          The kind of corresponding bean, handled by the backing
 *          {@link PageableCollection} instance.
 */
// TODO olaf: control/ensure that the PM factory releases the PMs for the beans that are no longer on the current page.
public class PageablePmBeanCollection<T_PM extends PmBean<T_BEAN>, T_BEAN> implements PageableCollection2<T_PM> {

  /** The collection type specific selection handler. */
  private final SelectionHandler<T_PM> selectionHandler;

  private PmObject                     pmCtxt;
  private PageableCollection2<T_BEAN>  beans;

  /**
   * Creates a collection backed by the given {@link PageableCollection} of beans.
   *
   * @param pmCtxt
   *          The PM context used to create the PM's for the bean items.
   * @param pageableBeanCollection
   *          The collection of beans to represent by this collection of bean-PM's.
   */
  public PageablePmBeanCollection(PmObject pmCtxt, PageableCollection2<T_BEAN> pageableBeanCollection) {
    assert pmCtxt != null;
    assert pageableBeanCollection != null;

    this.pmCtxt = pmCtxt;
    this.beans = pageableBeanCollection;
    this.selectionHandler = new SelectionHandlerWithPmFactory<T_PM, T_BEAN>(pmCtxt, pageableBeanCollection.getSelectionHandler());
  }

  /**
   * Creates a collection backed by a {@link PageableListImpl}.
   *
   * @param pmCtxt
   *          The PM context used to create the PM's for the bean items.
   * @param beans
   *          The set of beans to handle.
   * @param queryOptions
   *          the optional query options.
   */
  public PageablePmBeanCollection(PmObject pmCtxt, Collection<T_BEAN> beans, QueryOptions queryOptions) {
	  this(pmCtxt, beans, queryOptions, null);
  }

  /**
   * Creates a collection backed by a {@link PageableListImpl}.
   *
   * @param pmCtxt
   *          The PM context used to create the PM's for the bean items.
   * @param beans
   *          The set of beans to handle.
   * @param queryOptions
   *          the optional query options.
   * @param query
   *          the used query. Is optional.
   */
  public PageablePmBeanCollection(PmObject pmCtxt, Collection<T_BEAN> beans, QueryOptions queryOptions, QueryParams query) {
    this(pmCtxt,
         new PageableInMemCollectionImpl<T_BEAN>(
             new InMemPmQueryEvaluator<T_BEAN>(pmCtxt),
             beans,
             queryOptions,
             query)
        );
  }

  /**
   * Creates a collection backed by a {@link PageableQueryService}.
   *
   * @param pmCtxt
   *          the PM context used to create the PM's for the bean items.
   * @param service
   *          provides the beans and query options.
   * @param query
   *          the optional query to use.
   */
  public <T_ID extends Serializable> PageablePmBeanCollection(PmObject pmCtxt, PageableQueryService<T_BEAN, T_ID> service, QueryParams query) {
    this(pmCtxt, new PageableQueryCollection<T_BEAN, T_ID>(service, query));
  }

  public <T_ID extends Serializable> PageablePmBeanCollection(PmObject pmCtxt, PageableQueryService<T_BEAN, T_ID> service) {
    this(pmCtxt, service, null);
  }

  /**
   * Creates a collection backed by a {@link PageableInMemCollectionImpl} with no query constraints.
   *
   * @param pmCtxt
   *          The PM context used to create the PM's for the bean items.
   * @param beans
   *          The set of beans to handle.
   */
  public PageablePmBeanCollection(PmObject pmCtxt, Collection<T_BEAN> beans) {
    this(pmCtxt, beans, new QueryOptions(), null);
  }

  @Override
  public QueryParams getQueryParams() {
    return beans.getQueryParams();
  }

  @Override
  public QueryOptions getQueryOptions() {
    return beans.getQueryOptions();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_PM> getItemsOnPage() {
    return (List<T_PM>) PmFactoryApi.getPmListForBeans(pmCtxt, beans.getItemsOnPage(), false);
  }

  @Override
  public int getPageSize() {
    return beans.getPageSize();
  }

  @Override
  public void setPageSize(int newSize) {
    beans.setPageSize(newSize);
  }

  @Override
  public int getCurrentPageIdx() {
    return beans.getCurrentPageIdx();
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    beans.setCurrentPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return beans.getNumOfItems();
  }

  @Override
  public long getUnfilteredItemCount() {
    return beans.getUnfilteredItemCount();
  }

  @Override
  public Iterator<T_PM> iterator() {
    final Iterator<T_BEAN> beanIter = beans.iterator();
    return new Iterator<T_PM>() {
      @Override
      public boolean hasNext() {
        return beanIter.hasNext();
      }
      @Override
      public T_PM next() {
        T_BEAN b = beanIter.next();
        return b != null
            ? (T_PM) PmFactoryApi.<T_BEAN, T_PM>getPmForBean(pmCtxt, b)
            : null;
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public SelectionHandler<T_PM> getSelectionHandler() {
    return selectionHandler;
  }

  @Override
  public void clearCaches() {
    beans.clearCaches();
  }

}
