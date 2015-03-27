package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmTabSetImpl;

/**
 * Special details handler concerning tab set related behavior
 *
 * @author Dietmar Zabel
 * @param <T_DETAILS_PM>
 * @param <T_MASTER_RECORD>
 */
public class DetailsPmTabSetHandler<T_DETAILS_PM extends PmTabSetImpl, T_MASTER_RECORD> extends DetailsPmHandlerImpl<T_DETAILS_PM, T_MASTER_RECORD> {

    /**
     * Constructor
     * @param detailsPm
     */
    public DetailsPmTabSetHandler(T_DETAILS_PM detailsPm) {
        super(detailsPm);
    }

    /**
     * The new master bean should be shown on a useful tab
     *
     * @param newMasterBean
     */
    @Override
    protected void afterMasterRecordChangeImpl(T_MASTER_RECORD newMasterBean) {
        super.afterMasterRecordChangeImpl(newMasterBean);
        detailsPm.invalidateNotUsefulCurrentTab();
    }
}
