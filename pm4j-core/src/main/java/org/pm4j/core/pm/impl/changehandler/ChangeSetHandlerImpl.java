package org.pm4j.core.pm.impl.changehandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.pm4j.core.exception.PmRuntimeException;
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
public class ChangeSetHandlerImpl<T_ITEM extends PmDataInput> implements ChangeSetHandler<T_ITEM>{

  /** The PM to observe the child item change states for. */
  private final PmObject observedRootPm;

  /** The set of modified rows. */
  private Map<PmObject, ChangeKind> changedItemPms = new IdentityHashMap<PmObject, ChangeKind>();

  /** Listens for changed state changes in the subtree and updates the changed items accordingly. */
  private PmChangeListener itemHierarchyChangeListener = new PmChangeListener();

  /** A collection of details handlers that report changes in relation to the master handled by this instance. */
  private Collection<MasterPmHandler> detailsPmHandlers = new ArrayList<MasterPmHandler>();

  /**
   * @param observedRootPm The PM to observe the child item change states for.
   * @param itemPmClass The class of the child items to observe.
   */
  public ChangeSetHandlerImpl(PmObject observedRootPm) {
    this.observedRootPm = observedRootPm;

    PmEventApi.addHierarchyListener(observedRootPm, PmEvent.VALUE_CHANGED_STATE_CHANGE, itemHierarchyChangeListener);

    // If the observed containter (table) has to display other values, the current changed state gets obsolete.
    PmEventApi.addPmEventListener(observedRootPm, PmEvent.VALUE_CHANGE, new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        if (event.getValueChangeKind().isContentReplacingChangeKind()) {
          clearChanges();
        }
      }
    });
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
  @Override
  public boolean registerChange(ChangeKind changeKind, T_ITEM item) {
    boolean oldIsChanged = isChanged();
    switch(changeKind) {
      case ADD:
        changedItemPms.put(item, changeKind);
        break;
      case UPDATE:
        if (changedItemPms.get(item) == null) {
          changedItemPms.put(item, changeKind);
        }
        break;
      case DELETE:
        ChangeKind registeredRowChange = changedItemPms.get(item);
        // If the registered change for the item was an ADD then this change was only undone.
        // Thus this is not a delete of an original (persistent) item.
        if (registeredRowChange == ChangeKind.ADD) {
          changedItemPms.remove(item);
        } else {
          changedItemPms.put(item, changeKind);
        }
        break;
      default: throw new PmRuntimeException(observedRootPm, "Unhandled change kind: " + changeKind);
    }
    return (oldIsChanged != isChanged());
  }

  @Override
  public boolean isChanged() {
    return ! changedItemPms.isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<T_ITEM> getChangedItems(ChangeKind... changeKinds) {
    if (changedItemPms.isEmpty()) {
      return Collections.EMPTY_LIST;
    }

    if (changeKinds.length == 0) {
      return (Collection<T_ITEM>)(Object)changedItemPms.keySet();
    }

    Collection<ChangeKind> changeKindColleciton = Arrays.asList(changeKinds);
    Collection<T_ITEM> resultSet = new ArrayList<T_ITEM>();

    for (Map.Entry<PmObject, ChangeKind> e : changedItemPms.entrySet()) {
      if (changeKindColleciton.contains(e.getValue())) {
        resultSet.add((T_ITEM)e.getKey());
      }
    }
    return resultSet;
  }

  /**
   * Resets the handler to an unchanged state.
   */
  public void clearChanges() {
    changedItemPms.clear();
  }

  /**
   * Adds a {@link MasterPmHandler} to consider for changed state information.
   *
   * @param detailsPmHandler
   */
  public void addDetailsPmHandler(MasterPmHandler detailsPmHandler) {
    detailsPmHandlers.add(detailsPmHandler);
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
      @SuppressWarnings("unchecked")
      T_ITEM itemPm = (T_ITEM) findChildItemToObserve(event.pm);

      if (itemPm != null) {
        ChangeKind registeredRowChange = changedItemPms.get(itemPm);

        if (registeredRowChange == null) {
          // A value change event get thrown even if the value was set back to
          // its original value.
          // Thus we have to double check if the item is in a changed state now.
          if (itemPm.isPmValueChanged()) {
            registerChange(ChangeKind.UPDATE, itemPm);
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

}
