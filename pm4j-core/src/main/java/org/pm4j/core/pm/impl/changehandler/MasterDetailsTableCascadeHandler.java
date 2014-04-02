package org.pm4j.core.pm.impl.changehandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.modifications.ModificationsImpl;
import org.pm4j.common.modifications.ModificationsUtil;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable.UpdateAspect;
import org.pm4j.core.pm.impl.PmTableImpl;

public class MasterDetailsTableCascadeHandler<T_MASTER_BEAN, T_DETAILS_BEAN> extends DetailsPmHandlerImpl<PmTable<?>, T_MASTER_BEAN> {

  private Map<T_MASTER_BEAN, ModificationsImpl<T_DETAILS_BEAN>> masterBeanToDetailsModificationsMap = new HashMap<T_MASTER_BEAN, ModificationsImpl<T_DETAILS_BEAN>>();

  /** A temporary storage used to remember the modifications till the next {@link #afterMasterRecordChange(Object)} call. */
  private Modifications<T_DETAILS_BEAN> beforeSwitchModifications;
  /** A temporary storage used to remember the old master record till the next {@link #afterMasterRecordChange(Object)} call. */
  private T_MASTER_BEAN beforeSwitchMasterBean;
  /** Reference to the latest master bean. */
  private T_MASTER_BEAN currentMasterBean;

  public MasterDetailsTableCascadeHandler(PmTableImpl<?, ? extends T_DETAILS_BEAN> detailsPm) {
    super(detailsPm);
  }

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
    if (currentModifications.isModified()) {
      updateModificationsMapForMasterBean(currentMasterBean, currentModifications);
    }
    return (Map<T_MASTER_BEAN, Modifications<T_DETAILS_BEAN>>) (Object) Collections.unmodifiableMap(masterBeanToDetailsModificationsMap);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected boolean beforeMasterRecordChangeImpl(T_MASTER_BEAN oldMasterBean) {
    boolean canDo =  super.beforeMasterRecordChangeImpl(oldMasterBean);
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

    /**
     * Resets changed states and selections from the details table.<br>
     * But preserves it's sort order and filter settings.
     */
    PmTableImpl<?, ?> detailsTablePm = getDetailsTable();
    detailsTablePm.clearMasterRowPm();
    // the next getSelection call ensures then the minimal selection state.
    detailsTablePm.getPmPageableCollection().getSelectionHandler().ensureSelectionStateRequired();
    detailsTablePm.updatePmTable(UpdateAspect.CLEAR_CHANGES, UpdateAspect.CLEAR_SELECTION);
  }

  /** @return A typed reference to the details table. */
  @SuppressWarnings("unchecked")
  protected PmTableImpl<?, ? extends T_DETAILS_BEAN> getDetailsTable() {
    return (PmTableImpl<?, ? extends T_DETAILS_BEAN>) getDetailsPm();
  }

  private void updateModificationsMapForMasterBean(T_MASTER_BEAN masterBean, Modifications<T_DETAILS_BEAN> detailsModifications) {
    if (masterBean == null) {
      return;
    }

    ModificationsImpl<T_DETAILS_BEAN> oldModifications = masterBeanToDetailsModificationsMap.get(masterBean);
    ModificationsImpl<T_DETAILS_BEAN> newModifications = ModificationsUtil.joinModifications(oldModifications, detailsModifications);

    if (newModifications.isModified()) {
      masterBeanToDetailsModificationsMap.put(masterBean, newModifications);
    } else {
      masterBeanToDetailsModificationsMap.remove(masterBean);
    }
  }

}
