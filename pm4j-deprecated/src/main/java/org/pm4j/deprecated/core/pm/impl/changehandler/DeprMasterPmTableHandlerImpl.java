package org.pm4j.deprecated.core.pm.impl.changehandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.selection.EmptySelection;
import org.pm4j.common.selection.Selection;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.changehandler.DetailsPmHandler;
import org.pm4j.core.pm.impl.changehandler.DetailsPmHandlerImpl;
import org.pm4j.core.pm.impl.changehandler.MasterPmHandler;
import org.pm4j.deprecated.core.pm.DeprPmTable;
import org.pm4j.deprecated.core.pm.DeprPmTable.TableChange;

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
 * @deprecated please use {@link PmTable}
 */
@Deprecated
public class DeprMasterPmTableHandlerImpl<T_MASTER_BEAN> implements MasterPmHandler {

  private static final Logger    LOG                = LoggerFactory.getLogger(DeprMasterPmTableHandlerImpl.class);

  private final DeprPmTable<?>    masterTablePm;
  private Set<T_MASTER_BEAN>  changedMasterBeans = new HashSet<T_MASTER_BEAN>();
  private List<DetailsPmHandler> detailsHandlers = new ArrayList<DetailsPmHandler>();
  private PmCommandDecorator masterSelectionChangeDecorator;

  /**
   * Creates an instance uing a {@link DetailsPmHandler} that is responsible
   * for handling the details changes after a master record change.
   *
   * @param masterTablePm
   *          The table PM to observe.
   * @param detailsHandler A handler for the details PM
   */
  public DeprMasterPmTableHandlerImpl(DeprPmTable<?> masterTablePm, DetailsPmHandler... detailsHandlers) {
    this.masterTablePm = masterTablePm;
    for (DetailsPmHandler dh : detailsHandlers) {
      addDetailsHander(dh);
    }
  }

  /**
   * Creates for each passed details PM a details-default handler ({@link DetailsPmHandlerImpl})
   * that just clears the details area on master record change.
   *
   * @param masterTablePm
   *          The table to observe.
   * @param detailsPms The details PM's to observe.
   */
  public DeprMasterPmTableHandlerImpl(DeprPmTable<?> masterTablePm, PmObject... detailsPms) {
    this.masterTablePm = masterTablePm;
    for (PmObject pm : detailsPms) {
      addDetailsHander(new DetailsPmHandlerImpl<PmObject, T_MASTER_BEAN>(pm));
    }
  }

  /**
   * Provides a bridge to the new master handler interface.
   */
  @Override
  public Modifications<T_MASTER_BEAN> getMasterBeanModifications() {
    return new Modifications<T_MASTER_BEAN>() {
      @Override
      public boolean isModified() {
        return (!changedMasterBeans.isEmpty()) || isCurrentDetailsAreaChanged();
      }

      /** The set of added items is currently not relevant for the old master details scenarios. */
      @Override
      public List<T_MASTER_BEAN> getAddedItems() {
        return Collections.emptyList();
      }

      @Override
      public Collection<T_MASTER_BEAN> getUpdatedItems() {
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

      /** The set of removed items is currently not relevant for the old master details scenarios. */
      @Override
      public Selection<T_MASTER_BEAN> getRemovedItems() {
        return EmptySelection.getEmptySelection();
      }
    };
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
    masterTablePm.addDecorator(getMasterSelectionChangeDecorator(), TableChange.SELECTION);
    PmEventApi.addPmEventListener(masterTablePm, PmEvent.VALUE_CHANGE, makeTableValueChangeListener());
  }

  /**
   * Checks the current state of the details area PMs.
   *
   * @return <code>true</code> if one details area returns <code>true</code> for the call <code>isPmValueChanged()</code>.
   */
  public boolean isCurrentDetailsAreaChanged() {
    for (DetailsPmHandler dh : detailsHandlers) {
      PmObject detail = dh.getDetailsPm();
      if (detail != null &&
          detail.isPmValueChanged()) {
        return true;
      }
    }
    // no registerd change and no current change found in details areas:
    return false;
  }

  public T_MASTER_BEAN getSelectedMasterBean() {
    @SuppressWarnings("unchecked")
    PmBean<T_MASTER_BEAN> selectedRow = (PmBean<T_MASTER_BEAN>) masterTablePm.getCurrentRowPm();
    return selectedRow != null
        ? selectedRow.getPmBean()
        : null;
  }

  @Override
  public PmObject getMasterPm() {
    return masterTablePm;
  }

  /**
   * Sets the handler to an 'unchanged' state by clearing the set of {@link #changedMasterBeans}.
   * Re-adjusts the details area by calling {@link DetailsPmHandler#afterMasterRecordChange(Object)}
   * with the new selected table row.
   *
   * @param event the master PM value change event.
   */
  protected void onMasterTableValueChange(PmEvent event) {
    if (LOG.isDebugEnabled() && getMasterBeanModifications().isModified()) {
      LOG.debug("Propagate successful master-details change for " + PmUtil.getPmLogString(masterTablePm));
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
  public PmCommandDecorator makeTableSelectionChangeDecorator() {
    return new MasterSelectionChangeListener();
  }

  /**
   * Checks if it is allowed to swich to another master record.
   * <p>
   * The default implementation allows the switch if there is no currently selected master
   * record.<br>
   * If that's the case it checks if the details area is valid.
   *
   * @return <code>true</code> if the switch can be performed.
   */
  protected boolean canSwitch() {
    @SuppressWarnings("unchecked")
    PmBean<Object> rowPm = (PmBean<Object>) masterTablePm.getCurrentRowPm();

    if (rowPm == null) {
      return true;
    }

    if (!validateDetails()) {
      return false;
    }

    for (DetailsPmHandler dh : detailsHandlers) {
      // If you need the second parameter in the DetailsPmHandler
      // please use MasterPmTableHandlerImpl.
      if (!dh.beforeMasterRecordChange(rowPm.getPmBean(), null)) {
        return false;
      }
    }

    return true;
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
    for (DetailsPmHandler dh : detailsHandlers) {
      PmObject d = dh.getDetailsPm();
      if (d != null &&
          !PmValidationApi.validateSubTree(d)) {
        allDetailsValid = false;
      }
    }

    return allDetailsValid;
  }

  protected DeprPmTable<?> getMasterTablePm() {
    return masterTablePm;
  }

  @Override
  public final boolean beforeMasterSelectionChange() {
    return getMasterSelectionChangeDecorator().beforeDo(null);
  }

  @Override
  public final void afterMasterSelectionChange() {
    getMasterSelectionChangeDecorator().afterDo(null);
  }

  private PmCommandDecorator getMasterSelectionChangeDecorator() {
    if (masterSelectionChangeDecorator == null) {
      masterSelectionChangeDecorator = makeTableSelectionChangeDecorator();
    }
    return masterSelectionChangeDecorator;
  }

  /**
   * A decorator that prevents a value change if the details area is not valid
   * and sets the new details area if the change was executed.<br>
   * It also registers the master records that where changed within the details area.
   */
  protected class MasterSelectionChangeListener implements PmCommandDecorator {
    private T_MASTER_BEAN changedMasterBean;

    @Override
    public boolean beforeDo(PmCommand cmd) {
      if (canSwitch()) {
        T_MASTER_BEAN masterBean = getSelectedMasterBean();
        changedMasterBean =
            (masterBean != null) && isCurrentDetailsAreaChanged()
            ? masterBean
            : null;

        return true;
      }

      return false;
    }

    @Override
    public void afterDo(PmCommand cmd) {
      if (changedMasterBean != null &&
          !changedMasterBeans.contains(changedMasterBean)) {
        changedMasterBeans.add(changedMasterBean);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Registered a master-details change for " + PmUtil.getPmLogString(masterTablePm) +
              ". Changed bean: " + changedMasterBean);
        }
      }

      T_MASTER_BEAN selectedMasterBean = getSelectedMasterBean();
      for (DetailsPmHandler dh : detailsHandlers) {
        dh.afterMasterRecordChange(selectedMasterBean);
      }
      changedMasterBean = null;
    }
  }

}
