package org.pm4j.core.pm.filter;

/**
 * Filter base class with generic item parameter type support.
 *
 * @author olaf boede
 * @param <T_ITEM> Type of the items to filter.
 */
public abstract class FilterBase<T_ITEM> implements Filter {

  @SuppressWarnings("unchecked")
  @Override
  public final boolean doesItemMatch(Object item) {
    return doesItemMatchImpl((T_ITEM)item);
  }

  protected abstract boolean doesItemMatchImpl(T_ITEM item);

}
