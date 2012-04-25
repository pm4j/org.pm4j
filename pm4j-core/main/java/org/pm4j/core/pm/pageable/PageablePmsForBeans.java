package org.pm4j.core.pm.pageable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmFactoryApi;

/**
 * A {@link PageableItems} instance that provides {@link PmBean} instances in
 * front of a {@link PageableItems} container that handles the corresponding
 * bean instances.
 *
 * @author olaf boede
 *
 * @param <T_PM>
 *          The kind of {@link PmBean} provided by this class.
 * @param <T_BEAN>
 *          The kind of corresponding bean, handled by the backing
 *          {@link PageableItems} instance.
 */
// TODO olaf: control/ensure that the PM factory releases the PMs for the beans that are no longer on the current page.
public class PageablePmsForBeans<T_PM extends PmBean<T_BEAN>, T_BEAN> implements PageableCollection<T_PM> {

  private PmObject                   pmCtxt;
  private PageableCollection<T_BEAN> beans;

  public PageablePmsForBeans(PmTable<T_PM> pmCtxt, PageableCollection<T_BEAN> beanItems) {
    this.pmCtxt = pmCtxt;
    this.beans = beanItems;
  }

  public PageablePmsForBeans(PmTable<T_PM> pmCtxt, Collection<T_BEAN> beans) {
	    this(pmCtxt, new PageableListImpl<T_BEAN>(beans));
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

  @SuppressWarnings("unchecked")
  @Override
  public void sortItems(Comparator<?> sortComparator) {
    beans.sortItems(sortComparator != null
        ? new BeanComparatorBasedOnPmComparator<T_PM, T_BEAN>(
              (Comparator<T_PM>)sortComparator)
        : null);
  }

  @Override
  public void sortBackingItems(Comparator<?> sortComparator) {
    beans.sortItems(sortComparator);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setItemFilter(Filter<?> filter) {
    beans.setItemFilter(filter != null
        ? new BeanFilterBasedOnPmFilter<T_PM, T_BEAN>(
              (Filter<T_PM>)filter)
        : null);
  }

  @Override
  public void setBackingItemFilter(Filter<?> filter) {
    this.beans.setItemFilter(filter);
  }

  @Override
  public Filter<?> getBackingItemFilter() {
    return beans.getBackingItemFilter();
  }

  @Override
  public int getNumOfItems() {
    return beans.getNumOfItems();
  }

  @Override
  public boolean isSelected(T_PM item) {
    return item != null
            ? beans.isSelected(item.getPmBean())
            : null;
  }

  @Override
  public void select(T_PM item) {
    boolean wasUnselected = !isSelected(item);
    beans.select(item != null
            ? item.getPmBean()
            : null);
    // fire the event after successful select
    if(wasUnselected) {
      PmEventApi.firePmEventIfInitialized(pmCtxt, PmEvent.SELECTION_CHANGE);
    }
  }

  @Override
  public void deSelect(T_PM item) {
    boolean wasSelected = isSelected(item);
    beans.deSelect(item != null
        ? item.getPmBean()
        : null);
    if(wasSelected) {
      PmEventApi.firePmEventIfInitialized(pmCtxt, PmEvent.SELECTION_CHANGE);
    }
  }

  @Override
  public boolean isMultiSelect() {
    return beans.isMultiSelect();
  }

  @Override
  public void setMultiSelect(boolean isMultiSelect) {
    beans.setMultiSelect(isMultiSelect);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<T_PM> getSelectedItems() {
    return (Collection<T_PM>) PmFactoryApi.getPmListForBeans(pmCtxt, beans.getSelectedItems(), false);
  }

  @Override
  public void onUpdateCollection() {
    beans.onUpdateCollection();
  }

  public PageableCollection<T_BEAN> getBeans() {
    return beans;
  }

  public void setBeans(PageableCollection<T_BEAN> pageableBeanCollection) {
    this.beans = pageableBeanCollection;
  }

  /**
   * A comparator that can compare backing beans based on another comparator
   * that compares the corresponding PMs.
   *
   * @param <T_ITEM_PM>
   * @param <T_ITEM>
   */
  class BeanComparatorBasedOnPmComparator<T_ITEM_PM extends PmBean<T_ITEM>, T_ITEM> implements Comparator<T_ITEM> {

    private final Comparator<T_ITEM_PM> pmComparator;

    public BeanComparatorBasedOnPmComparator(Comparator<T_ITEM_PM> pmComparator) {
      this.pmComparator = pmComparator;
    }

    @Override
    public int compare(T_ITEM o1, T_ITEM o2) {
      T_ITEM_PM pm1 = PmFactoryApi.<T_ITEM, T_ITEM_PM>getPmForBean(pmCtxt, o1);
      T_ITEM_PM pm2 = PmFactoryApi.<T_ITEM, T_ITEM_PM>getPmForBean(pmCtxt, o2);
      return pmComparator.compare(pm1, pm2);
    }
  }

  /**
   * A filter that can compare backing beans based on another filter
   * that compares the corresponding PMs.
   *
   * @param <T_ITEM_PM>
   * @param <T_ITEM>
   */
  class BeanFilterBasedOnPmFilter<T_ITEM_PM extends PmBean<T_ITEM>, T_ITEM> implements Filter<T_ITEM> {

    private final Filter<T_ITEM_PM> pmFilter;

    public BeanFilterBasedOnPmFilter(Filter<T_ITEM_PM> pmFilter) {
      this.pmFilter = pmFilter;
    }

    public boolean doesItemMatch(T_ITEM item) {
      T_ITEM_PM itemPm = PmFactoryApi.<T_ITEM, T_ITEM_PM>getPmForBean(pmCtxt, item);
      return pmFilter.doesItemMatch(itemPm);
    }

  }

}
