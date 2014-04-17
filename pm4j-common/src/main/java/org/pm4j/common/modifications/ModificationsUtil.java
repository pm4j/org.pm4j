package org.pm4j.common.modifications;

import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionSet;

public class ModificationsUtil {

  // TODO oboede: move to modification handler!
  // At least the type specific aggregation of removed items.
  public static <T_ITEM> ModificationsImpl<T_ITEM> joinModifications(ModificationsImpl<T_ITEM> modifications, Modifications<T_ITEM> modificationsToAdd) {
    if (modifications == null) {
      modifications = new ModificationsImpl<T_ITEM>();
    }

    if ((modificationsToAdd != null) && modificationsToAdd.isModified()) {
      for (T_ITEM i : modificationsToAdd.getAddedItems()) {
        modifications.registerAddedItem(i);
      }
      for (T_ITEM i : modificationsToAdd.getUpdatedItems()) {
        modifications.registerUpdatedItem(i, true);
      }

      @SuppressWarnings("unchecked")
      Selection<T_ITEM> removed = SelectionSet.aggregateSelections(modifications.getRemovedItems(), modificationsToAdd.getRemovedItems());
      modifications.setRemovedItems(removed);
    }
    return modifications;
  }
}
