package org.pm4j.core.pm.impl.changehandler;

import java.util.Collection;

/**
 * Implementations of  {@link ChangeSetHandler} may be used to observe changes
 * of collections.<br>
 * E.g. for a set of records handled by a table.
 *
 * @param <T_ITEM> the type of handled items.
 *
 * @author olaf boede
 */
public interface ChangeSetHandler<T_ITEM> {

  enum ChangeKind {
    ADD, UPDATE, DELETE
  }

  /**
   * Checks if something is changed within the observed scope.
   *
   * @return <code>true</code> if there is a registered change.
   */
  boolean isChanged();

  /**
   * Provides the changed items for the given change kind(s).
   *
   * @param changeKinds
   *          the change kinds to filter the items by.<br>
   *          All changes will be delivered if no change kind is passed.
   *
   * @return the items that have been registered for the given change kind.
   */
  Collection<T_ITEM> getChangedItems(ChangeKind... changeKinds);

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
   * TODO olaf: move to implementation level interface.
   *
   * Adds a {@link MasterPmHandler} to consider for changed state information.
   *
   * @param detailsPmHandler
   */
  void addDetailsPmHandler(MasterPmHandler detailsPmHandler);

}
