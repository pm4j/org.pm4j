package org.pm4j.core.pm.impl.changehandler;

import java.util.Collection;

import org.pm4j.common.selection.Selection;

public interface ChangeSet<T_ITEM> {

  enum ChangeKind {
    ADD, UPDATE
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
   * @return The set of deleted items.
   */
  Selection<T_ITEM> getDeletedItems();

}
