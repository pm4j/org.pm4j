package org.pm4j.core.pm.impl.changehandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A master-details handler for tables with an associated details PM that displays
 * additional information for the currently selected master table row. <br>
 * It supports the following functionality:
 * <ul>
 * <li>Listens for master table 'selection change' events.</li>
 * <li>Prevents the selection change if the details area is not valid.</li>
 * <li>Registers details area changes. See {@link #isChangeRegistered()}
 * and {@link #getChangedMasterBeans()}</li>
 * </ul>
 *
 * @param <T_MASTER_BEAN>
 *          Type of beans handled by the master PM.
 *
 * @author olaf boede
 */
public class MasterPmSelectionHandlerImpl<T_MASTER_BEAN> implements MasterPmRecordHandler<T_MASTER_BEAN> {

  private static final Log    LOG                = LogFactory.getLog(MasterPmSelectionHandlerImpl.class);

  private final PmObject masterPm;
  private final SelectionHandler<?> selectionHandler;
  private Set<T_MASTER_BEAN>  changedMasterBeans = new HashSet<T_MASTER_BEAN>();
  private List<DetailsPmHandler<?>> detailsHandlers = new ArrayList<DetailsPmHandler<?>>();

  /**
   * Creates an instance uing a {@link DetailsPmHandler} that is responsible
   * for handling the details changes after a master record change.
   *
   * @param masterTablePm
   *          The table PM to observe.
   * @param detailsHandler A handler for the details PM
   */
  public MasterPmSelectionHandlerImpl(PmObject masterPm, SelectionHandler<?> selectionHandler, DetailsPmHandler<?>... detailsHandlers) {
    this.masterPm = masterPm;
    this.selectionHandler = selectionHandler;
    for (DetailsPmHandler<?> dh : detailsHandlers) {
      addDetailsHander(dh);
    }
  }

  /**
   * Creates for each passed details PM a details-default handler ({@link DetailsPmObjectHandlerImpl})
   * that just clears the details area on master record change.
   *
   * @param masterTablePm
   *          The table to observe.
   * @param detailsPms The details PM's to observe.
   */
  public MasterPmSelectionHandlerImpl(PmObject masterPm, SelectionHandler<?> selectionHandler, PmObject... detailsPms) {
    this.masterPm = masterPm;
    this.selectionHandler = selectionHandler;
    for (PmObject pm : detailsPms) {
      addDetailsHander(new DetailsPmObjectHandlerImpl<PmObject, T_MASTER_BEAN>(pm));
    }
  }

  @Override
  public final void addDetailsHander(DetailsPmHandler<?> detailsHandler) {
    this.detailsHandlers.add(detailsHandler);
  }

  @Override
  public final void addDetailsHanders(DetailsPmHandler<?>... detailsHandlers) {
    this.detailsHandlers.addAll(Arrays.asList(detailsHandlers));
  }

  @Override
  public Collection<DetailsPmHandler<?>> getDetailsPmHandlers() {
    return detailsHandlers;
  }

  @Override
  public void startObservers() {
    selectionHandler.addPropertyAndVetoableListener(
        SelectionHandler.PROP_SELECTION,
        makeTableSelectionChangeDecorator()
        );
    PmEventApi.addPmEventListener(masterPm, PmEvent.VALUE_CHANGE, makeTableValueChangeListener());
  }

  /**
   * Checks first for already registered details-triggered master record changes.<br>
   * If none is found, the details areas are checked if they are actually changed.
   */
  @Override
  public boolean isChangeRegistered() {
    Set<T_MASTER_BEAN> changedMasterBeans = getChangedMasterBeans();
    if (changedMasterBeans != null && !changedMasterBeans.isEmpty()) {
      return true;
    }
    else {
      return isCurrentDetailsAreaChanged();
    }
  }

  /**
   * Checks the current state of the details area PMs.
   *
   * @return <code>true</code> if one details area returns <code>true</code> for the call <code>isPmValueChanged()</code>.
   */
  protected boolean isCurrentDetailsAreaChanged() {
    for (DetailsPmHandler<?> dh : detailsHandlers) {
      Object detail = dh.getDetailsPm();
      if ((detail instanceof PmDataInput) &&
          ((PmDataInput)detail).isPmValueChanged()) {
        return true;
      }
    }
    // no registerd change and no current change found in details areas:
    return false;
  }

  protected T_MASTER_BEAN getSelectedMasterBean() {
    Selection<T_MASTER_BEAN> beanSelection = selectionHandler.getSelection().getBeanSelection();
    return beanSelection.getSize() == 1
        ? beanSelection.iterator().next()
        : null;
  }

  @Override
  public Set<T_MASTER_BEAN> getChangedMasterBeans() {
    T_MASTER_BEAN masterBean = getSelectedMasterBean();
    if (masterBean != null &&
        isCurrentDetailsAreaChanged()) {
      HashSet<T_MASTER_BEAN> set = new HashSet<T_MASTER_BEAN>(changedMasterBeans);
      if (!changedMasterBeans.contains(masterBean)) {
        set.add(masterBean);
      }
      return set;
    } else {
      return changedMasterBeans;
    }
  }

  /**
   * Sets the handler to an 'unchanged' state by clearing the set of {@link #changedMasterBeans}.
   * Re-adjusts the details area by calling {@link DetailsPmHandler#afterMasterRecordChange(Object)}
   * with the new selected table row.
   *
   * @param event the master PM value change event.
   */
  protected void onMasterTableValueChange(PmEvent event) {
    if (LOG.isDebugEnabled() && isChangeRegistered()) {
      LOG.debug("Reset master-details changed state for " + PmUtil.getPmLogString(masterPm));
    }

    switch (event.getValueChangeKind()) {
      case RELOAD: // fall through
      case VALUE:
        changedMasterBeans.clear();
      default: // nothing to do
    }

    T_MASTER_BEAN selectedMasterBean = getSelectedMasterBean();
    for (DetailsPmHandler<?> dh : detailsHandlers) {
      dh.afterMasterRecordChange(selectedMasterBean);
    }
  }

  /**
   * Provides a listener that resets the registered changes in case of a table
   * value change (other records to handle).
   *
   * @return The listener.
   */
  protected PmEventListener makeTableValueChangeListener() {
    return new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        onMasterTableValueChange(event);
      }
    };
  }

  /**
   * A decorator that prevents a value change if the details area is not valid
   * and sets the new details area if the change was executed.<br>
   * It also registers the changes of the details area.
   *
   * @return The decorator.
   */
  protected PropertyAndVetoableChangeListener makeTableSelectionChangeDecorator() {
    return new MasterSelectionChangeListener();
  }

  /**
   * Checks if it is allowed to swich to another master record.
   * <p>
   * The default implementation allows the switch if there is no currently selected master
   * record.<br>
   * If that's tha case it checks if the details area is valid.
   *
   * @return <code>true</code> if the switch can be performed.
   */
  protected boolean canSwitch() {
    return (selectionHandler.getSelection().getSize() != 1)
        ? true
        : validateDetails();
  }

  /**
   * Validates all registered details PMs.
   * <p>
   * Validates all details areas, even if the first one is already invalid.
   * This way the user gets all relevant validation messages for the user operation.
   *
   * @return <code>true</code> if all details areas are valid.
   */
  protected boolean validateDetails() {
    boolean allDetailsValid = true;
    for (DetailsPmHandler<?> dh : detailsHandlers) {
      Object d = dh.getDetailsPm();
      if ((d instanceof PmDataInput) &&
          !PmValidationApi.validateSubTree((PmDataInput)d)) {
        allDetailsValid = false;
      }
    }

    return allDetailsValid;
  }

  /**
   * A decorator that prevents a value change if the details area is not valid
   * and sets the new details area if the change was executed.<br>
   * It also registers the master records that where changed within the details area.
   */
  protected class MasterSelectionChangeListener implements PropertyAndVetoableChangeListener {
    private T_MASTER_BEAN changedMasterBean;

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      if (!canSwitch()) {
        throw new PropertyVetoException("MasterPmSelectionHandler prevents switch", evt);
      }

      T_MASTER_BEAN masterBean = getSelectedMasterBean();
      changedMasterBean =
          (masterBean != null) && isCurrentDetailsAreaChanged()
          ? getCurrentModifiedMasterRecord()
          : null;
   }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (changedMasterBean != null &&
          !changedMasterBeans.contains(changedMasterBean)) {
        changedMasterBeans.add(changedMasterBean);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Registered a master-details change. Changed bean: " + changedMasterBean);
        }
      }

      T_MASTER_BEAN selectedMasterBean = getSelectedMasterBean();
      for (DetailsPmHandler<?> dh : detailsHandlers) {
        dh.afterMasterRecordChange(selectedMasterBean);
      }
      changedMasterBean = null;
    }
  }

  protected PmObject getMasterPm() {
    return masterPm;
  }

  /**
   * @return The currently selected master record in case it is marked a actually changed within the details area. May be <code>null</code>.
   */
  private T_MASTER_BEAN getCurrentModifiedMasterRecord() {
    return isCurrentDetailsAreaChanged()
        ? getSelectedMasterBean()
        : null;
  }


}
