package org.pm4j.core.pm.filter;

/**
 * Filter base class with generic item parameter type support.
 *
 * @author olaf boede
 * @param <T_ITEM> Type of the items to filter.
 */
public abstract class FilterBase<T_ITEM> implements Filter {

  public enum FilterKind { ITEM_FILTER, BEAN_FILTER }

  private boolean beanFilter;

  public FilterBase(FilterKind filterKind) {
    this.beanFilter = (filterKind == FilterKind.BEAN_FILTER);
  }

  protected abstract boolean doesItemMatchImpl(T_ITEM item);

  @SuppressWarnings("unchecked")
  @Override
  public final boolean doesItemMatch(Object item) {
    return doesItemMatchImpl((T_ITEM)item);
  }

  @Override
  public final boolean isBeanFilter() {
    return beanFilter;
  }

}
