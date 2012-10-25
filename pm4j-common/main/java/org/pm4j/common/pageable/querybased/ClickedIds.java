package org.pm4j.common.pageable.querybased;

import java.util.Collection;

/**
 * An quite internal collection of individually selected or unselected items.
 * <p>
 * In case of an inverted selection it contains the manually de-selected items.
 *
 * @author OBOEDE
 *
 * @param <T_ID>
 */
public class ClickedIds<T_ID> {

  private final Collection<T_ID> ids;
  private final boolean invertedSelection;

  /**
   *
   * @param ids the set of individually selected/deselected item id's.
   * @param invertedSelection inverted selection indicator.
   */
  public ClickedIds(Collection<T_ID> ids, boolean invertedSelection) {
    this.ids = ids;
    this.invertedSelection = invertedSelection;
  }

  /**
   * @return the set of individually selected/deselected item id's.
   */
  public Collection<T_ID> getIds() {
    return ids;
  }

  /**
   * @return inverted selection indicator.
   */
  public boolean isInvertedSelection() {
    return invertedSelection;
  }

}
