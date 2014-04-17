package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.core.pm.PmObject;

/**
 * Master-details handling can be very details PM specific.<br>
 * Implementations of this handler may consider this.
 *
 * @author Olaf Boede
 *
 * @param <T_DETAILS_PM>
 *          Type of the supported details PM.
 */
public interface DetailsPmHandler {

  /**
   * Provides the details area PM to handle.
   *
   * @return The details PM to handle.
   */
  PmObject getDetailsPm();

  /** @deprecated Please use {@link #beforeMasterRecordChange(Object)}. */
  @Deprecated
  boolean canSwitchMasterRecord();

  /**
   * Performs the checks and logic to be applied before the master selection may be switched
   * from the given master record to another one.
   *
   * @param oldMasterBean The master record to deselect.
   * @return <code>true</code> if this handler agrees to the switch. <code>false</code> prevents the switch.
   */
  boolean beforeMasterRecordChange(Object oldMasterBean);

  /**
   * Gets called after a master record selection change.
   * <p>
   * In case of a master table the provided object may be the object behind the
   * selected table row.<br>
   * In case of a master combo box (PM layer: PmAttr with options) the provided
   * object may represent the selected combo box item.
   *
   * @param newMasterBean
   *          The new master record. May be <code>null</code>.
   */
  void afterMasterRecordChange(Object newMasterBean);

}