package org.pm4j.common.pageable.querybased.pagequery;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * An quite internal collection of individually selected or unselected items.
 * <p>
 * In case of an inverted selection it contains the manually de-selected items.
 * <p>
 * Inverted selection is a synonym for 'select all'.
 *
 * @author olaf boede
 *
 * @param <T_ID>
 */
public class ClickedIds<T_ID> implements Serializable {

  private static final long serialVersionUID = 1L;

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

  /** Creates an empty clicked id set. */
  @SuppressWarnings("unchecked")
  public ClickedIds() {
    this(Collections.EMPTY_LIST, false);
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
