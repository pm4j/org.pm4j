package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.core.pm.impl.PmTableImpl;

public class MasterDetailsPmTableHandler<T_MASTER_BEAN, T_DETAILS_BEAN> extends DetailsPmTableHandlerBase<T_MASTER_BEAN, T_DETAILS_BEAN>{

  private final PmTableImpl<?, ? extends T_MASTER_BEAN> masterTablePm;

  public MasterDetailsPmTableHandler(PmTableImpl<?, ? extends T_MASTER_BEAN> masterTablePm, PmTableImpl<?, ? extends T_DETAILS_BEAN> detailsTablePm) {
    super(detailsTablePm);
    assert masterTablePm != null : "masterTablePm should not be null";

    this.masterTablePm = masterTablePm;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Modifications<T_MASTER_BEAN> getMasterModifications() {
    return (Modifications<T_MASTER_BEAN>) masterTablePm.getPmPageableBeanCollection().getModifications();
  }

}
