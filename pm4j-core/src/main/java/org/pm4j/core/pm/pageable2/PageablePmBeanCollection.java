package org.pm4j.core.pm.pageable2;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.inmem.PageableInMemCollectionImpl;
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
  private final SelectionHandlerWithPmFactory<T_PM, T_BEAN> selectionHandler;

  private PmObject                     pmCtxt;
  private PageableCollection2<T_BEAN>  beanCollection;

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
    this.beanCollection = pageableBeanCollection;
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
    return beanCollection.getQueryParams();
  }

  @Override
  public QueryOptions getQueryOptions() {
    return beanCollection.getQueryOptions();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_PM> getItemsOnPage() {
    return (List<T_PM>) PmFactoryApi.getPmListForBeans(pmCtxt, beanCollection.getItemsOnPage(), false);
  }

  @Override
  public int getPageSize() {
    return beanCollection.getPageSize();
  }

  @Override
  public void setPageSize(int newSize) {
    beanCollection.setPageSize(newSize);
  }

  @Override
  public int getCurrentPageIdx() {
    return beanCollection.getCurrentPageIdx();
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    beanCollection.setCurrentPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return beanCollection.getNumOfItems();
  }

  @Override
  public long getUnfilteredItemCount() {
    return beanCollection.getUnfilteredItemCount();
  }

  @Override
  public Iterator<T_PM> iterator() {
    final Iterator<T_BEAN> beanIter = beanCollection.iterator();
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

  @SuppressWarnings("unchecked")
  @Override
  public SelectionHandler<T_BEAN> getBeanSelectionHandler() {
    return selectionHandler.getBeanSelectionHandler();
  }

  @Override
  public void clearCaches() {
    beanCollection.clearCaches();
  }

  @Override
  public void addItem(T_PM item) {
    beanCollection.addItem(item.getPmBean());
  }

  /**
   * Provides the pageable collection of beans behind this collection of bean-PMs.
   *
   * @return the bean collection.
   */
  public PageableCollection2<T_BEAN> getBeanCollection() {
    return beanCollection;
  }
}
