package org.pm4j.core.pm.impl.changehandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.modifications.Modifications;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmValidationApi;

/**
 * A registry that observes changes of items within parent PM.
 * <p>
 * Current restriction: Works only if all items belong to the same class.
 * E.g. PmTableRowPm for
 * It may work if all items
 *
 * @author OBOEDE
 *
 * @deprecated please use {@link ModificationHandler} and {@link Modifications}.
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

  /**
   * This parallel collection is a quick workaround for the missing sort order of added items.
   */
  private List<PmObject> addedItemPms = new ArrayList<PmObject>();

  private boolean recordsDeleted = false;

  /** Listens for changed state changes in the subtree and updates the changedRows accordingly. */
  private PmChangeListener itemHierarchyChangeListener = new PmChangeListener();

  private Collection<MasterPmHandler> detailsPmHandlers = new ArrayList<MasterPmHandler>();

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

  /**
   * Adds a {@link MasterPmHandler} to consider for changed state information.
   *
   * @param detailsPmHandler
   */
  public void addDetailsPmHandler(MasterPmHandler detailsPmHandler) {
    detailsPmHandlers.add(detailsPmHandler);
  }

  /**
   * Checks if something is changed within the observed scope.
   *
   * @return <code>true</code> if there is a registered change.
   */
  public boolean isAChangeRegistered() {
    return recordsDeleted ||
           ! changedItemPms.isEmpty() ||
           isDetailsChangeRegistered();
  }

  protected PmObject findChildItemToObserve(PmObject changedItem) {
    if (changedItem == observedRootPm) {
      return null;
    }

    PmObject p = changedItem;
    do {
      if (p.getPmParent() == observedRootPm) {
        // FIXME olaf: this does not correctly identify the intended children
        // (in case of tables we need to make sure that we only observe rows...)
        return p;
      }
      p = p.getPmParent();
    }
    while (p != null);

    // the changed item is not a child of the observed PM.
    return null;
  }

  private boolean isDetailsChangeRegistered() {
    for (MasterPmHandler d : detailsPmHandlers) {
      if (d.getMasterBeanModifications().isModified()) {
        return true;
      }
    }
    // no change
    return false;
  }

  /** Listens for changed state changes in the subtree and updates the changedRows accordingly. */
  private class PmChangeListener implements PmEventListener {
    @Override
    public void handleEvent(PmEvent event) {
      PmObject itemPm = findChildItemToObserve(event.pm);

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
    PmValidationApi.clearInvalidValuesOfSubtree(observedRootPm);
    changedItemPms.clear();
    addedItemPms.clear();
    recordsDeleted = false;
  }

  public void onAddNewItem(PmObject newItemPm) {
    changedItemPms.put(newItemPm, CHANGE.ADD);
    addedItemPms.add(newItemPm);
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
    addedItemPms.remove(deletedItem);
  }

  public Collection<PmObject> getChangedItems() {
    return changedItemPms.keySet();
  }

  public List<PmObject> getAddedItems() {
    return addedItemPms;
  }

}
