package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.core.pm.PmTabSet;
import org.pm4j.core.pm.impl.PmTabSetImpl;

/**
 * Special details handler concerning {@link PmTabSet} related behavior.
 * <p>
 * On master record switch it
 * <ul>
 * <li>clears any cached information and messages (logic is provided by
 * DetailsPmHandlerImpl)</li>
 * <li>resets the current tab if it got invisible or disabled</li>
 * </ul>
 *
 * @author Dietmar Zabel
 * @param <T_MASTER_RECORD>
 *          The master record type used as parameter for
 *          <code>before/afterMasterRecordSwitchImpl()</code>.
 */
public class DetailsPmTabSetHandler<T_MASTER_RECORD> extends DetailsPmHandlerImpl<PmTabSetImpl, T_MASTER_RECORD> {

    /**
     * Constructor
     * @param detailsPm
     */
    public DetailsPmTabSetHandler(PmTabSetImpl detailsPm) {
        super(detailsPm);
    }

    /**
     * The new master bean should be shown on a useful tab
     *
     * @param newMasterBean
     */
    @Override
    protected void afterMasterRecordChangeImpl(T_MASTER_RECORD oldMasterBean, T_MASTER_RECORD newMasterBean) {
        super.afterMasterRecordChangeImpl(oldMasterBean, newMasterBean);
        getTypedDetailsPm().resetCurrentTabPmIfInactive();
    }
}
