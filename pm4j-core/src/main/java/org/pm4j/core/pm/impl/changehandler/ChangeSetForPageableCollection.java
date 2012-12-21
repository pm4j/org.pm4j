package org.pm4j.core.pm.impl.changehandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.selection.Selection;

public abstract class ChangeSetForPageableCollection<T_ITEM> implements ChangeSet<T_ITEM> {

  @Override
  public boolean isChanged() {
    return getPageableCollection().getModificationHandler().isModified();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<T_ITEM> getChangedItems(org.pm4j.core.pm.impl.changehandler.ChangeSet.ChangeKind... changeKinds) {
    return Arrays.asList(changeKinds).contains(ChangeKind.ADD)
        ? getPageableCollection().getModificationHandler().getAddedItems()
        : Collections.EMPTY_LIST;
  }

  @Override
  public Selection<T_ITEM> getDeletedItems() {
    return getPageableCollection().getModificationHandler().getRemovedItems();
  }

  protected abstract PageableCollection2<T_ITEM> getPageableCollection();
}
