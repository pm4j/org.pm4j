package org.pm4j.core.pm.impl.changehandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;

import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.core.pm.impl.PmTableImpl;

/**
 * A master-details handler that uses a {@link PmTableImpl} as master.
 *
 * @param <T_MASTER_BEAN>
 *          the type of master beans behind the master table rows.
 *
 * @author Olaf Boede
 */
public class MasterPmTableHandlerImpl<T_MASTER_BEAN> extends MasterPmHandlerImpl<T_MASTER_BEAN> {

  /**
   * @param masterPm
   *          the table of master records.
   * @param detailsHandlers
   *          the dependent detail area handlers.
   */
  public MasterPmTableHandlerImpl(PmTableImpl<?, T_MASTER_BEAN> masterPm, DetailsPmHandler... detailsHandlers) {
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
        PageableCollection.EVENT_REMOVE_SELECTION, new MasterRecordRemoveListener());
  }

  /**
   * Provides the master row bean of the table.<br>
   * In some scenarios the master may be different from the 'selected' row.
   * E.g. in case of a multi selection with an active master row.
   */
  @Override
  protected T_MASTER_BEAN getSelectedMasterBean() {
    return getMasterTablePm().getMasterRowPmBean();
  }

  /**
   * Type safe access method.
   *
   * @return The master table.
   */
  @SuppressWarnings("unchecked")
  protected PmTableImpl<?, T_MASTER_BEAN> getMasterTablePm() {
    return (PmTableImpl<?, T_MASTER_BEAN>) getMasterPm();
  }

  /**
   * A listener that informs the details in the event of a master record delete operation.
   * <p>
   * It remembers the selected master record before the delete operation gets executed
   * and informs the detail handlers after successful delete operation execution.<br>
   * This will only be done if the master record was really part of the deleted record selection.
   */
  public class MasterRecordRemoveListener implements PropertyAndVetoableChangeListener {

    private WeakReference<T_MASTER_BEAN> masterBeanBeforeDeleteOperation;

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      T_MASTER_BEAN b = getSelectedMasterBean();
      masterBeanBeforeDeleteOperation = (b != null)
          ? new WeakReference<T_MASTER_BEAN>(getSelectedMasterBean())
          : null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      T_MASTER_BEAN masterBeanBeforeDeleteOperation = this.masterBeanBeforeDeleteOperation != null ? this.masterBeanBeforeDeleteOperation.get() : null;

      PmTableImpl<?, T_MASTER_BEAN> masterTablePm = getMasterTablePm();
      masterTablePm.clearMasterRowPm();

      @SuppressWarnings("unchecked")
      Selection<T_MASTER_BEAN> deletedBeansSelection = (Selection<T_MASTER_BEAN>) evt.getOldValue();
      if ((masterBeanBeforeDeleteOperation != null) &&
          deletedBeansSelection.contains(masterBeanBeforeDeleteOperation)) {
        // after clearing all selection information an auto-select will fire a selection change
        // event that triggers a master selection change for all details handlers.
        T_MASTER_BEAN newMasterBean = masterTablePm.getMasterRowPmBean();

        // only on case of a null selection the details handlers need a additional info about the
        // change
        if (newMasterBean == null) {
          for (DetailsPmHandler dh : getDetailsPmHandlers()) {
            dh.afterMasterRecordChange(masterBeanBeforeDeleteOperation, newMasterBean);
          }
        }
      }
    }

  }

}
