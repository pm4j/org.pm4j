package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.core.pm.impl.PmTableImpl;

/**
 * A details change handler for the combination of a master table and a details table.
 *
 * @author Olaf Boede
 *
 * @param <T_MASTER_BEAN> The master bean type.
 * @param <T_DETAILS_BEAN> The details bean type.
 */
public class DetailsPmTableToTableHandler<T_MASTER_BEAN, T_DETAILS_BEAN> extends DetailsPmTableHandlerBase<T_MASTER_BEAN, T_DETAILS_BEAN>{

  private final PmTableImpl<?, ? extends T_MASTER_BEAN> masterTablePm;

  public DetailsPmTableToTableHandler(PmTableImpl<?, ? extends T_MASTER_BEAN> masterTablePm, PmTableImpl<?, ? extends T_DETAILS_BEAN> detailsTablePm) {
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
