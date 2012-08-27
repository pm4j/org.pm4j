package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.core.pm.PmBean;

/**
 * Base class logic extension: {@link #afterMasterRecordChangeImpl(Object)}
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

  @Override
  protected void afterMasterRecordChangeImpl(T_MASTER_RECORD newMasterBean) {
    getDetailsPm().setPmBean(newMasterBean);
  }

}
