package org.pm4j.core.util.filter;

import java.util.List;

import org.pm4j.common.util.collection.IterableUtil;

/**
 * TOCOMMENT:
 * 
 * @param <T_ITEM>
 */
public class PassThroughFilter<T_ITEM> extends ItemFilter<T_ITEM> {

  @Override
  public boolean acceptItem(T_ITEM item) {
    return true;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Provides always a shallow copy of the given iterable to ensure that both
   * sets are decoupled. That is the same behaviour that the user gets from the
   * other Filter implementations too.
   */
  @Override
  public List<T_ITEM> filter(Iterable<T_ITEM> unfilteredList) {
    return IterableUtil.shallowCopy(unfilteredList);
  }

}
