package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.core.pm.PmBean;

/**
 * Base class logic extension: {@link #afterMasterRecordChangeImpl(Object, Object)}
 * adjusts the details area by calling <code>setPmBean</code> using the new
 * master bean.
 *
 * @author olaf boede
 */
public class DetailsPmBeanHandlerImpl<T_MASTER_RECORD> extends
    DetailsPmHandlerImpl<PmBean<T_MASTER_RECORD>, T_MASTER_RECORD> {

  /**
   * @param detailsPm The details PM to handle.
   */
  public DetailsPmBeanHandlerImpl(PmBean<T_MASTER_RECORD> detailsPm) {
    super(detailsPm);
  }

  /**
   * Checks directly if the currently handled bean is the new master bean.
   * This is currently more reliable than comparing the old and new bean parameters.
   *
   * TODO: May be changed back to the default implementation when the number of selection change events
   * got reduced to the required minimum.
   */
  @Override
  protected boolean shouldProcessMasterChange(Object oldMasterBean, Object newMasterBean) {
    return getTypedDetailsPm().getPmBean() != newMasterBean;
  }

  @Override
  protected void afterMasterRecordChangeImpl(T_MASTER_RECORD oldMasterBean, T_MASTER_RECORD newMasterBean) {
    getTypedDetailsPm().setPmBean(newMasterBean);
  }

}
