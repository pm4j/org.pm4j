package org.pm4j.core.pm.impl.changehandler;

import java.util.Collection;

/**
 * Implementations of  {@link ChangeSetRegistry} may be used to observe changes
 * of collections.<br>
 * E.g. for a set of records handled by a table.
 *
 * @param <T_ITEM> the type of handled items.
 *
 * @author olaf boede
 */
public interface ChangeSetRegistry<T_ITEM> {

  enum ChangeKind {
    ADD, UPDATE, DELETE
  }

  /**
   * Registers an observed change.
   *
   * @param changeKind
   *          the observed change kind.
   * @param item
   *          the changed item.
   * @return <code>true</code> if the result of {@link #isChanged()} was also
   *         changed by this operation.
   */
  boolean registerChange(ChangeKind changeKind, T_ITEM item);

  /**
   * Checks if something is changed within the observed scope.
   *
   * @return <code>true</code> if there is a registered change.
   */
  boolean isChanged();

  /**
   * Provides the changed items for the given change kind.
   *
   * @param changeKind
   *          the change kind to filter the items by.
   * @return the items that have been registered for the given change kind.
   */
  Collection<T_ITEM> getChangedItems(ChangeKind changeKind);

  /**
   * Resets the handler to an unchanged state.
   */
  void clearChanges();

}
