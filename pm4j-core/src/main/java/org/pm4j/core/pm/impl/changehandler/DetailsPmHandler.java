package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmObject;

/**
 * Master-details handling can be very details PM specific.<br>
 * Implementations of this handler may consider this.
 *
 * @author olaf boede
 *
 * @param <T_DETAILS_PM>
 *          Type of the supported details PM.
 */
public interface DetailsPmHandler<T_DETAILS_PM extends PmObject> {

  /**
   * Provides the details area PM to handle.
   *
   * @return The details PM to handle.
   */
  T_DETAILS_PM getDetailsPm();

  /**
   * The details handler can prevent a master record switch by returning here
   * <code>false</code>.
   * <p>
   * A typical implementation: The master record can only be switched if the
   * details area is valid.
   *
   * @return <code>true</code> if the master record can be switched.
   */
  boolean canSwitchMasterRecord();

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

  /**
   * Some {@link MasterPmHandler} call this method to inform the details areas
   * about a master record delete operation.
   * <p>
   * This allows detail handlers to handle the details changes that where
   * recorded in relation to the given master record.
   *
   * @param deletedMasterBean
   */
  void afterMasterRecordDelete(Object deletedMasterBean);

  /**
   * Adds a decorator to consider in {@link #canSwitchMasterRecord()} and {@link #afterMasterRecordChange(Object)}.
   *
   * @param decorator
   */
  void addDecorator(PmCommandDecorator decorator);

  /**
   * Provides all detail bean changes grouped by their master beans.
   *
   * @return a map to a {@link ChangeSetHandler} that provides
   */
  // TODO olaf: check.
  // Map<T_MASTER_BEAN, ChangeSetHandler<T_DETAILS_BEAN>> getChangeSetHandler();

}