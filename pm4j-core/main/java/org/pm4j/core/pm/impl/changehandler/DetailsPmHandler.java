package org.pm4j.core.pm.impl.changehandler;

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