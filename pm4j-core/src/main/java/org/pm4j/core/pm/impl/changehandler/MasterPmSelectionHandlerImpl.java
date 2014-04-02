package org.pm4j.core.pm.impl.changehandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.modifications.Modifications;
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
public abstract class MasterPmSelectionHandlerImpl<T_MASTER_BEAN> implements MasterPmHandler {

  private static final Log    LOG                = LogFactory.getLog(MasterPmSelectionHandlerImpl.class);

  private final PmObject masterPm;
  private final SelectionHandler<T_MASTER_BEAN> selectionHandler;
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

  /**
   * Provides the modification handler that gets informed in case of a details change.
   *
   * @return the modification handler.
   */
  protected abstract ModificationHandler<T_MASTER_BEAN> getModificationHandler();

  @Override
  public Modifications<T_MASTER_BEAN> getMasterBeanModifications() {
    return getModificationHandler().getModifications();
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
    // observe master record selection change events.
    selectionHandler
        .addPropertyAndVetoableListener(SelectionHandler.PROP_SELECTION, getMasterSelectionChangeListener());

    // Observe master table content change events.
    // After complete processing of the masterPm value change event the details PMs
    // will get a message about the potential master record change.
    // This prevents conflicts between the changed state handling and the master record
    // selection change logic.
    PmEventApi.addPmEventListener(masterPm, PmEvent.VALUE_CHANGE, new PmEventListener() {
      PostProcessor<Object> postProcessor = new PostProcessor<Object>() {
        @Override
        public void postProcess(PmEvent event, Object postProcessPayload) {
          afterMasterSelectionChange();
        }
      };

      @Override
      public void handleEvent(PmEvent event) {
        event.addPostProcessingListener(postProcessor, null);
      }
    });

    // A listener that reacts on pure VALUE_CHANGE events as they
    // get triggered by <b>user data entry events</b>.
    PmEventListener el = new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        // Only propagations of pure sub-PM value changes should be considered.
        // Combined events, such as PmEvent.ALL_CHANGE_EVENTS, are not relevant for user data
        // entry modification tracking.
        if ((event.getChangeMask() == (PmEvent.VALUE_CHANGE | PmEvent.IS_EVENT_PROPAGATION)) &&
            isCurrentDetailsAreaChanged()) {
          T_MASTER_BEAN selectedMasterBean = getSelectedMasterBean();
          registerDetailsChangeForMasterBean(selectedMasterBean);
        }
      }
    };

    for (DetailsPmHandler dh : detailsHandlers) {
      PmEventApi.addHierarchyListener(dh.getDetailsPm(), PmEvent.VALUE_CHANGE, el);
    }

    // adjust the details areas by informing them about the initial master bean.
    T_MASTER_BEAN selectedMasterBean = getSelectedMasterBean();
    if (selectedMasterBean != null) {
      for (DetailsPmHandler dh : detailsHandlers) {
        dh.afterMasterRecordChange(selectedMasterBean);
      }
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

  /**
   * Re-adjusts the details area by calling
   * {@link DetailsPmHandler#afterMasterRecordChange(Object)} with the new
   * selected table row.
   */
  public void afterMasterSelectionChange() {
    if (LOG.isDebugEnabled() && getMasterBeanModifications().isModified()) {
      LOG.debug("Propagate successful master-details change for " + PmUtil.getPmLogString(masterPm));
    }

    T_MASTER_BEAN selectedMasterBean = getSelectedMasterBean();
    for (DetailsPmHandler dh : detailsHandlers) {
      dh.afterMasterRecordChange(selectedMasterBean);
    }
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
   * {@link DetailsPmHandler#beforeMasterRecordChange(Object)}.
   * <p>
   * All details areas get processed, even if the first one is already invalid.
   * This way the user gets all relevant validation messages for the user
   * operation.
   *
   * @return <code>true</code> if the switch can be performed.
   */
  public boolean beforeSwitch() {
    T_MASTER_BEAN b = getSelectedMasterBean();
    if (b == null) {
      return true;
    }

    boolean allDetailsAgree = true;
    for (DetailsPmHandler dh : detailsHandlers) {
      if (!dh.beforeMasterRecordChange(b)) {
        allDetailsAgree = false;
      }
    }
    return allDetailsAgree;
  }

  // FIXME olaf: check if we can simply call beforeSwitch... a duplicate control flow...
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

  @Override
  public void afterDo(PmCommand cmd) {
    afterMasterSelectionChange();
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
  public class MasterSelectionChangeListener implements PropertyAndVetoableChangeListener {
      private T_MASTER_BEAN changedMasterBean;

      @Override
      public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (!beforeSwitch()) {
          throw new PropertyVetoException("MasterPmSelectionHandler prevents switch", evt);
        }

        T_MASTER_BEAN masterBean = getSelectedMasterBean();
        changedMasterBean =
              (masterBean != null) && isCurrentDetailsAreaChanged()
                   ? masterBean
                   : null;
    }

    /**
     * This method gets called <b>after</b> a successful master record selection change.
     * <p>
     * If there was a change registered for the previous master record, this gets registered
     * within the set of changed master beans.
     * <p>
     * Informs all details handlers about the selection change.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      registerDetailsChangeForMasterBean(changedMasterBean);
      afterMasterSelectionChange();

      changedMasterBean = null;
    }
  }

  /**
   * Gets called whenever a details change was observed that causes the master
   * record to be marked as modified.
   *
   * @param masterBean the master bean the details change was observed for.
   */
  protected void registerDetailsChangeForMasterBean(T_MASTER_BEAN masterBean) {
    if (masterBean != null) {
      getModificationHandler().registerUpdatedItem(masterBean, true);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Registered a details change for master bean: " + masterBean);
      }
    }
  }

  @Override
  public PmObject getMasterPm() {
    return masterPm;
  }

}
