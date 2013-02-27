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
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAspectChangeCommandImpl;
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
	private final SelectionHandler<T_MASTER_BEAN> selectionHandler;
	private Set<T_MASTER_BEAN>  changedMasterBeans = new HashSet<T_MASTER_BEAN>();
	private List<DetailsPmHandler> detailsHandlers = new ArrayList<DetailsPmHandler>();
	private PropertyAndVetoableChangeListener masterSelectionChangeListener;

	/**
	 * Creates an instance uing a {@link DetailsPmHandler} that is responsible
	 * for handling the details changes after a master record change.
	 *
	 * @param masterTablePm
	 *          The table PM to observe.
	 * @param detailsHandler A handler for the details PM
	 */
	public MasterPmSelectionHandlerImpl(PmObject masterPm, SelectionHandler<T_MASTER_BEAN> selectionHandler, DetailsPmHandler... detailsHandlers) {
		assert masterPm != null;
		assert selectionHandler != null;

		this.masterPm = masterPm;
		this.selectionHandler = selectionHandler;
		for (DetailsPmHandler dh : detailsHandlers) {
			addDetailsHander(dh);
		}
	}

  @Override
  public final void addDetailsHander(DetailsPmHandler detailsHandler) {
    this.detailsHandlers.add(detailsHandler);
  }

  @Override
  public final void addDetailsHanders(DetailsPmHandler... detailsHandlers) {
    this.detailsHandlers.addAll(Arrays.asList(detailsHandlers));
  }

  @Override
  public Collection<DetailsPmHandler> getDetailsPmHandlers() {
    return detailsHandlers;
  }

  @Override
  public void startObservers() {
    selectionHandler
        .addPropertyAndVetoableListener(SelectionHandler.PROP_SELECTION, makeTableSelectionChangeListener());
    PmEventApi.addPmEventListener(masterPm, PmEvent.VALUE_CHANGE, makeTableValueChangeListener());

    // adjust the details areas by informing them about the initial master bean.
    T_MASTER_BEAN selectedMasterBean = getSelectedMasterBean();
    if (selectedMasterBean != null) {
      for (DetailsPmHandler dh : detailsHandlers) {
        dh.afterMasterRecordChange(selectedMasterBean);
      }
    }
  }

  /**
   * Checks first for already registered details-triggered master record
   * changes.<br>
   * If none is found, the details areas are checked if they are actually
   * changed.
   */
  @Override
  public boolean isChangeRegistered() {
    Set<T_MASTER_BEAN> changedMasterBeans = getChangedMasterBeans();
    if (changedMasterBeans != null &&
        !changedMasterBeans.isEmpty()) {
      return true;
    } else {
      return isCurrentDetailsAreaChanged();
    }
  }

  /**
   * Checks the current state of the details area PMs.
   *
   * @return <code>true</code> if one details area returns <code>true</code> for
   *         the call <code>isPmValueChanged()</code>.
   */
  protected boolean isCurrentDetailsAreaChanged() {
    for (DetailsPmHandler dh : detailsHandlers) {
      Object detail = dh.getDetailsPm();
      if ((detail instanceof PmDataInput) && ((PmDataInput) detail).isPmValueChanged()) {
        return true;
      }
    }
    // no registerd change and no current change found in details areas:
    return false;
  }

  /**
   * Provides the currently selected master bean.
   * The default implementation just checks if the size of the selection
   * is one.
   *
   * @return the selected bean or <code>null</code>.
   */
  protected T_MASTER_BEAN getSelectedMasterBean() {
    Selection<T_MASTER_BEAN> beanSelection = selectionHandler.getSelection();
    return (beanSelection.getSize() == 1)
        ? beanSelection.iterator().next()
        : null;
  }

  @Override
  public Set<T_MASTER_BEAN> getChangedMasterBeans() {
    T_MASTER_BEAN masterBean = getSelectedMasterBean();
    if (masterBean != null && isCurrentDetailsAreaChanged()) {
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
   * Sets the handler to an 'unchanged' state by clearing the set of
   * {@link #changedMasterBeans}. Re-adjusts the details area by calling
   * {@link DetailsPmHandler#afterMasterRecordChange(Object)} with the new
   * selected table row.
   *
   * @param event
   *          the master PM value change event.
   */
  public void onMasterTableValueChange(PmEvent event) {
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
    for (DetailsPmHandler dh : detailsHandlers) {
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
  protected PropertyAndVetoableChangeListener makeTableSelectionChangeListener() {
    return new MasterSelectionChangeListener();
  }

  /**
   * Checks if it is allowed to swich to another master record.
   * <p>
   * Validates all registered detail areas by calling
   * {@link DetailsPmHandler#canSwitchMasterRecord()}.
   * <p>
   * All details areas get processed, even if the first one is already invalid.
   * This way the user gets all relevant validation messages for the user
   * operation.
   *
   * @return <code>true</code> if the switch can be performed.
   */
  public boolean canSwitch() {
    if (getSelectedMasterBean() == null) {
      return true;
    }

    boolean allDetailsValid = true;
    for (DetailsPmHandler dh : detailsHandlers) {
      if (!dh.canSwitchMasterRecord()) {
        allDetailsValid = false;
      }
    }
    return allDetailsValid;
  }

  // TODO olaf: check if we can simply call canSwitch...
  @Override
  public boolean beforeDo(PmCommand cmd) {
    PropertyAndVetoableChangeListener l = getMasterSelectionChangeListener();
    try {
      Object oldValue = null, newValue = null;
      if (cmd instanceof PmAspectChangeCommandImpl) {
        oldValue = ((PmAspectChangeCommandImpl) cmd).getOldValue();
        newValue = ((PmAspectChangeCommandImpl) cmd).getNewValue();
      }

      l.vetoableChange(new PropertyChangeEvent(getMasterPm(), SelectionHandler.PROP_SELECTION, oldValue, newValue));
      return true;
    } catch (PropertyVetoException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Selection change for " + getMasterPm().getPmRelativeName() + " was prevented by a veto: "
            + e.getMessage());
      }
      return false;
    }
  }

  // XXX olaf: translates to a property change event as it would be thrown by
  // the selection handler.
  @Override
  public void afterDo(PmCommand cmd) {
    PropertyAndVetoableChangeListener l = getMasterSelectionChangeListener();
    l.propertyChange(new PropertyChangeEvent(getMasterPm(), SelectionHandler.PROP_SELECTION, null, null));
  }

  private PropertyAndVetoableChangeListener getMasterSelectionChangeListener() {
    if (masterSelectionChangeListener == null) {
      masterSelectionChangeListener = makeTableSelectionChangeListener();
    }
    return masterSelectionChangeListener;
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
      for (DetailsPmHandler dh : detailsHandlers) {
        dh.afterMasterRecordChange(selectedMasterBean);
      }
      changedMasterBean = null;
    }
  }

  @Override
  public PmObject getMasterPm() {
    return masterPm;
  }

  /**
   * @return The currently selected master record in case it is marked a
   *         actually changed within the details area. May be <code>null</code>.
   */
  private T_MASTER_BEAN getCurrentModifiedMasterRecord() {
    return isCurrentDetailsAreaChanged()
        ? getSelectedMasterBean()
        : null;
  }

}
