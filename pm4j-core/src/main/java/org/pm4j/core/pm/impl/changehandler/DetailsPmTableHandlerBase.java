package org.pm4j.core.pm.impl.changehandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.util.CloneUtil;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable.UpdateAspect;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmTableImpl;

/**
 * A details handler that tracks changes of a details table in relation to all
 * master records that where selected by the user.
 *
 * @author Olaf Boede
 *
 * @param <T_MASTER_BEAN> The type of master beans.
 * @param <T_DETAILS_BEAN> The type of details beans.
 */
public abstract class DetailsPmTableHandlerBase<T_MASTER_BEAN, T_DETAILS_BEAN> extends DetailsPmHandlerImpl<PmTable<?>, T_MASTER_BEAN> {

  private Map<T_MASTER_BEAN, Modifications<T_DETAILS_BEAN>> masterBeanToDetailsModificationsMap = new HashMap<T_MASTER_BEAN, Modifications<T_DETAILS_BEAN>>();

  /** A temporary storage used to remember the modifications till the next {@link #afterMasterRecordChange(Object)} call. */
  private Modifications<T_DETAILS_BEAN> beforeSwitchModifications;
  /** A temporary storage used to remember the old master record till the next {@link #afterMasterRecordChange(Object)} call. */
  private T_MASTER_BEAN beforeSwitchMasterBean;
  /** Reference to the latest master bean. */
  private T_MASTER_BEAN currentMasterBean;

  public DetailsPmTableHandlerBase(PmTableImpl<?, ? extends T_DETAILS_BEAN> detailsPm) {
    super(detailsPm);
  }

  @Override
  public void startObservers() {
    super.startObservers();
    PmEventApi.addPmEventListener(getDetailsPm().getPmParent(), PmEvent.ALL_CHANGE_EVENTS, new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        if (event.isAllChangedEvent() || event.isReloadEvent()) {
          onParentCtxtValueInitialization(event);
        }
      }
    });
  }

  /**
   * @return The {@link Modifications} registered for the master.
   */
  protected abstract Modifications<T_MASTER_BEAN> getMasterModifications();

  /**
   * Provides a master bean to details modifications map.
   *
   * @return the masterBeanToModificationsMap
   */
  @SuppressWarnings("unchecked")
  public Map<T_MASTER_BEAN, Modifications<T_DETAILS_BEAN>> getMasterBeanToDetailsModificationsMap() {
    // The modifications are registered on master record switch.
    // Because of that the map does not contain modifications done in the details area for the current
    // master.
    // This code checks if that is the case and registers the related modifications.
    Modifications<T_DETAILS_BEAN> currentModifications = (Modifications<T_DETAILS_BEAN>) getDetailsTable().getPmPageableBeanCollection().getModifications();
    updateModificationsMapForMasterBean(currentMasterBean, currentModifications);
    return (Map<T_MASTER_BEAN, Modifications<T_DETAILS_BEAN>>) (Object) Collections.unmodifiableMap(masterBeanToDetailsModificationsMap);
  }

  @Override
  protected boolean beforeMasterRecordChangeImpl(T_MASTER_BEAN oldMasterBean, T_MASTER_BEAN newMasterBean) {
    boolean canDo =  super.beforeMasterRecordChangeImpl(oldMasterBean, newMasterBean);
    if (canDo) {
      this.beforeSwitchModifications = (Modifications<T_DETAILS_BEAN>) getDetailsTable().getPmPageableBeanCollection().getModifications();
      this.beforeSwitchMasterBean = oldMasterBean;
    } else {
      this.beforeSwitchModifications = null;
      this.beforeSwitchMasterBean = null;
    }
    return canDo;
  }

  @Override
  protected void afterMasterRecordChangeImpl(T_MASTER_BEAN newMasterBean) {
    super.afterMasterRecordChangeImpl(newMasterBean);
    currentMasterBean = newMasterBean;
    if (beforeSwitchModifications != null) {
      updateModificationsMapForMasterBean(beforeSwitchMasterBean, beforeSwitchModifications);
      beforeSwitchMasterBean = null;
      beforeSwitchModifications = null;
    }

    PmTableImpl<?, T_DETAILS_BEAN> detailsTablePm = getDetailsTable();
    detailsTablePm.clearMasterRowPm();
    // Resets changed states and selections from the details table.<br>
    // But preserves it's sort order and filter settings.
    detailsTablePm.updatePmTable(UpdateAspect.CLEAR_CHANGES, UpdateAspect.CLEAR_SELECTION);
    // Restores the last known details modification state.
    Modifications<T_DETAILS_BEAN> knownOldDetailsModifications = masterBeanToDetailsModificationsMap.get(currentMasterBean);
    if (knownOldDetailsModifications != null) {
      detailsTablePm.getPmPageableBeanCollection().getModificationHandler().setModifications(knownOldDetailsModifications);
    }
    // the next getSelection call ensures then the minimal selection state.
    detailsTablePm.getPmPageableCollection().getSelectionHandler().ensureSelectionStateRequired();
  }

  /**
   * Gets called whenever an initializing all-change event or a reload event
   * will be observed on the parent of the observed table PM.
   * <p>
   * The default implementation resets the internally handled state: The recorded details changes and the current master bean reference.
   *
   * @param event
   *          The received event.
   */
  protected void onParentCtxtValueInitialization(PmEvent event) {
    masterBeanToDetailsModificationsMap.clear();
    currentMasterBean = null;
    beforeSwitchMasterBean = null;
    beforeSwitchModifications = null;
  }

  /** @return A typed reference to the details table. */
  @SuppressWarnings("unchecked")
  protected PmTableImpl<?, T_DETAILS_BEAN> getDetailsTable() {
    return (PmTableImpl<?, T_DETAILS_BEAN>) getDetailsPm();
  }

  private void updateModificationsMapForMasterBean(T_MASTER_BEAN masterBean, Modifications<T_DETAILS_BEAN> newModifications) {
    if (masterBean == null) {
      return;
    }

    if (newModifications != null && newModifications.isModified()) {
      masterBeanToDetailsModificationsMap.put(masterBean, CloneUtil.clone(newModifications));
    } else {
      masterBeanToDetailsModificationsMap.remove(masterBean);
    }
  }

}
