package org.pm4j.core.pm.impl;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;

/**
 * A registry that observes changes of items within parent PM.
 * <p>
 * Current restriction: Works only if all items belong to the same class.
 * E.g. PmTableRowPm for
 * It may work if all items
 *
 * @author OBOEDE
 *
 */
public class ChangedChildStateRegistry {

  enum CHANGE {
    ADD,
    UPDATE
  }

  /** The PM to observe the child item change states for. */
  private final PmObject observedRootPm;

  /** The set of modified rows. */
  private Map<PmObject, CHANGE> changedItemPms = new IdentityHashMap<PmObject, CHANGE>();

  private boolean recordsDeleted = false;

  /** Listens for changed state changes in the subtree and updates the changedRows accordingly. */
  private PmChangeListener itemHierarchyChangeListener = new PmChangeListener();

  /**
   * @param observedRootPm The PM to observe the child item change states for.
   * @param itemPmClass The class of the child items to observe.
   */
  public ChangedChildStateRegistry(PmObject observedRootPm) {
    this.observedRootPm = observedRootPm;

    PmEventApi.addHierarchyListener(observedRootPm, PmEvent.VALUE_CHANGED_STATE_CHANGE, itemHierarchyChangeListener);

    // If the observed containter (table) has to display other values, the current changed state gets obsolete.
    PmEventApi.addPmEventListener(observedRootPm, PmEvent.VALUE_CHANGE, new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        if (event.getValueChangeKind().isContentReplacingChangeKind()) {
          clearChangedItems();
        }
      }
    });
  }

  public boolean isAChangeRegistered() {
    return recordsDeleted ||
           ! changedItemPms.isEmpty();
  }

  protected PmDataInput findChildItemToObserve(PmObject changedItem) {
    if (changedItem == observedRootPm) {
      return null;
    }

    PmObject p = changedItem;
    do {
      if (p.getPmParent() == observedRootPm) {
        // FIXME olaf: this does not correctly identify the intended children
        // (in case of tables we need to make sure that we only observe rows...)
        if (p instanceof PmDataInput) {
          return (PmDataInput)p;
        }
        else {
          return null;
        }
      }
      p = p.getPmParent();
    }
    while (p != null);

    // the changed item is not a child of the observed PM.
    return null;
  }

  /** Listens for changed state changes in the subtree and updates the changedRows accordingly. */
  private class PmChangeListener implements PmEventListener {
    @Override
    public void handleEvent(PmEvent event) {
      PmDataInput itemPm = findChildItemToObserve(event.pm);

      if (itemPm != null) {
        CHANGE registeredRowChange = changedItemPms.get(itemPm);

        if (registeredRowChange == null) {
          // A value change event get thrown even if the value was set back to
          // its original value.
          // Thus we have to double check if the item is in a changed state now.
          if (itemPm.isPmValueChanged()) {
            changedItemPms.put(itemPm, CHANGE.UPDATE);
          }
        }
        else {
          // a changed row will not be registered twice but it will be unregistered
          // if the row is no longer changed.
          if (!itemPm.isPmValueChanged()) {
            changedItemPms.remove(itemPm);
          }
        }
      }
    }
  }

  public void clearChangedItems() {
    changedItemPms.clear();
    recordsDeleted = false;
  }

  public void onAddNewItem(PmObject newItemPm) {
    changedItemPms.put(newItemPm, CHANGE.ADD);
  }

  public void onDeleteItem(PmObject deletedItem) {
    CHANGE registeredRowChange = changedItemPms.get(deletedItem);
    // If the registered change for the item was an ADD then this change was only undone.
    // Thus this is not a delete of an original (persistent) item.
    if (registeredRowChange != CHANGE.ADD) {
      recordsDeleted = true;
    }

    // Any change recorded for the deleted item is no longer influencing the changed state.
    changedItemPms.remove(deletedItem);
  }

  public Collection<PmObject> getChangedItems() {
    return changedItemPms.keySet();
  }

}
