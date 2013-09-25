package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.pageable.ItemNavigator;
import org.pm4j.common.selection.Selection;

/**
 * A navigator that uses a backing {@link PageableIdQueryService} to navigate over
 * a selection of items.
 *
 * @param <T> The item type.
 * @param <T_ID> The item ID type.
 *
 * @author oboede
 */
public class ItemNavigatorPageableIdQueryBased<T, T_ID> implements ItemNavigator<T> {

  /** Current navigator position. RecordNavigatorPageableQueryBased */
  private int currentItemIdx = 0;
  private final PageableIdQueryService<T, T_ID> service;
  private final List<T_ID> ids;
  private T cachedCurrentItem;

  public ItemNavigatorPageableIdQueryBased(PageableIdQueryCollectionImpl<T, T_ID> pgColl) {
    this(pgColl.getService(), pgColl.getSelection());
  }

  public ItemNavigatorPageableIdQueryBased(PageableIdQueryService<T, T_ID> service, Selection<T> selection) {
    this.service = service;
    ids = new ArrayList<T_ID>((int)selection.getSize());
    for (T t : selection) {
      T_ID id = service.getIdForItem(t);
      // XXX oboede: we could add support for transient items here in future.
      if (id == null) {
        throw new IllegalArgumentException("No ID found for item: " + t);
      }
      ids.add(id);
    }
  }

  @Override
  public T navigateTo(int itemPos) {
    currentItemIdx = itemPos;
    cachedCurrentItem = null;
    return getCurrentItem();
  }

  @Override
  public T getCurrentItem() {
    if (cachedCurrentItem == null) {
      T_ID id = ids.get(currentItemIdx);
      cachedCurrentItem = service.getItemForId(id);
    }
    return cachedCurrentItem;
  }

  @Override
  public int getNumOfItems() {
    return ids.size();
  }

  @Override
  public int getCurrentItemIdx() {
    return currentItemIdx;
  }

  @Override
  public void clearCaches() {
    cachedCurrentItem = null;
  }

}
