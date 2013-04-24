package org.pm4j.core.pm.impl.changehandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.pm4j.common.pageable.ModificationHandler;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.core.pm.impl.PmTableImpl2;

/**
 * A master-details handler that uses a {@link PmTableImpl2} as master.
 *
 * @param <T_MASTER_BEAN>
 *          the type of master beans behind the master table rows.
 *
 * @author olaf boede
 */
public class MasterPmTable2HandlerImpl<T_MASTER_BEAN> extends MasterPmSelectionHandlerImpl<T_MASTER_BEAN> {

  /**
   * @param masterPm
   *          the table of master records.
   * @param detailsHandlers
   *          the dependent detail area handlers.
   */
  public MasterPmTable2HandlerImpl(PmTableImpl2<?, T_MASTER_BEAN> masterPm, DetailsPmHandler[] detailsHandlers) {
    super(masterPm, masterPm.getPmPageableBeanCollection().getSelectionHandler(), detailsHandlers);
  }

  @Override
  protected ModificationHandler<T_MASTER_BEAN> getModificationHandler() {
    return getMasterTablePm().getPmPageableBeanCollection().getModificationHandler();
  }


  @Override
  public void startObservers() {
    super.startObservers();
    getMasterTablePm().getPmPageableBeanCollection().addPropertyAndVetoableListener(
        PageableCollection2.EVENT_REMOVE_SELECTION, new MasterRecordRemoveListener());
  }

  /**
   * Provides the 'current' row bean of the table.<br>
   * In some scenarions the 'current' may be different from the 'selected' row.
   * E.g. in case of a multi selection with a 'current' acive row.
   */
  @Override
  protected T_MASTER_BEAN getSelectedMasterBean() {
    return getMasterTablePm().getCurrentRowPmBean();
  }

  /**
   * Type safe access method.
   *
   * @return The master table.
   */
  @SuppressWarnings("unchecked")
  protected PmTableImpl2<?, T_MASTER_BEAN> getMasterTablePm() {
    return (PmTableImpl2<?, T_MASTER_BEAN>) getMasterPm();
  }

  /**
   * A listener that informs the details in the event of a master record delete operation.
   * <p>
   * It remembers the selected master record before the delete operation gets executed
   * and informs the detail handlers after successful delete operation execution.<br>
   * This will only be done if the master record was really part of the deleted record selection.
   */
  public class MasterRecordRemoveListener implements PropertyAndVetoableChangeListener {

    private T_MASTER_BEAN masterBeanBeforeDeleteOperation;

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      masterBeanBeforeDeleteOperation = getSelectedMasterBean();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      PmTableImpl2<?, T_MASTER_BEAN> masterTablePm = getMasterTablePm();
      masterTablePm.clearCurrentRowPmCache();
      @SuppressWarnings("unchecked")
      Selection<T_MASTER_BEAN> deletedBeansSelection = (Selection<T_MASTER_BEAN>) evt.getOldValue();
      if ((masterBeanBeforeDeleteOperation != null) &&
          deletedBeansSelection.contains(masterBeanBeforeDeleteOperation)) {
        T_MASTER_BEAN newMasterBean = masterTablePm.getCurrentRowPmBean();
        for (DetailsPmHandler dh : getDetailsPmHandlers()) {
          dh.afterMasterRecordChange(newMasterBean);
        }
      }
    }

  }

}
