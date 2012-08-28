package org.pm4j.core.pm.impl.changehandler;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable.TableChange;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A details handler for tables with an associated details PM that displays
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
public class MasterTableDetailsPmHandler<T_MASTER_BEAN> implements MasterDetailsPmHandler {

  private static final Log    LOG                = LogFactory.getLog(MasterTableDetailsPmHandler.class);

  private final PmTable<?>    masterTablePm;
  private Set<T_MASTER_BEAN>  changedMasterBeans = new HashSet<T_MASTER_BEAN>();
  private final DetailsPmHandler<? extends PmDataInput> detailsHandler;

  /**
   * Creates an instance uing a {@link DetailsPmHandler} that is responsible
   * for handling the details changes after a master record change.
   *
   * @param masterTablePm
   *          The table PM to observe.
   * @param detailsHandler A handler for the details PM
   */
  public MasterTableDetailsPmHandler(PmTable<?> masterTablePm, DetailsPmHandler<? extends PmDataInput> detailsHandler) {
    this.masterTablePm = masterTablePm;
    this.detailsHandler = detailsHandler;
  }

  /**
   * Creates an instance with a details-default handler ({@link DetailsPmHandlerImpl})
   * that just clears the details area on master record change.
   *
   * @param masterTablePm
   *          The table to observe.
   * @param detailsPm The details PM to observe.
   */
  public MasterTableDetailsPmHandler(PmTable<?> masterTablePm, PmDataInput detailsPm) {
    this(masterTablePm, new DetailsPmHandlerImpl<PmDataInput, T_MASTER_BEAN>(detailsPm));
  }

  @Override
  public void startObservers() {
    masterTablePm.addDecorator(makeTableSelectionChangeDecorator(), TableChange.SELECTION);
    PmEventApi.addPmEventListener(masterTablePm, PmEvent.VALUE_CHANGE, makeTableValueChangeListener());
  }

  @Override
  public boolean isChangeRegistered() {
    Set<T_MASTER_BEAN> changedMasterBeans = getChangedMasterBeans();
    return (changedMasterBeans != null && !changedMasterBeans.isEmpty()) ||
           detailsHandler.getDetailsPm().isPmValueChanged();
  }

  protected T_MASTER_BEAN getSelectedMasterBean() {
    @SuppressWarnings("unchecked")
    PmBean<T_MASTER_BEAN> selectedRow = (PmBean<T_MASTER_BEAN>) masterTablePm.getSelectedRow();
    return selectedRow != null
        ? selectedRow.getPmBean()
        : null;
  }

  @Override
  public Set<T_MASTER_BEAN> getChangedMasterBeans() {
    T_MASTER_BEAN masterBean = getSelectedMasterBean();
    if (masterBean != null &&
        detailsHandler.getDetailsPm().isPmValueChanged()) {
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
   * Sets the handler to an 'unchanged' state.
   */
  protected void onMasterTableValueChange() {
    if (LOG.isDebugEnabled() && isChangeRegistered()) {
      LOG.debug("Reset master-details changed state for " + PmUtil.getPmLogString(detailsHandler.getDetailsPm()));
    }
    changedMasterBeans.clear();
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
        onMasterTableValueChange();
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
  protected PmCommandDecorator makeTableSelectionChangeDecorator() {
    return new MasterSelectionChangeListener();
  }

  /**
   * Checks if it is allowed to swich to another master record.
   * <br>
   * The default implementation checks if the details area is valid.
   *
   * @return <code>true</code> if the switch can be performed.
   */
  protected boolean canSwitch() {
    return  masterTablePm.getSelectedRow() == null ||
            PmValidationApi.validateSubTree(detailsHandler.getDetailsPm());
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
            (masterBean != null) && detailsHandler.getDetailsPm().isPmValueChanged()
            ? getCurrentModifiedMasterRecord()
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
          LOG.debug("Registered a master-details change for " + PmUtil.getPmLogString(detailsHandler.getDetailsPm()) +
              ". Changed bean: " + changedMasterBean);
        }
      }

      detailsHandler.afterMasterRecordChange(getSelectedMasterBean());
      changedMasterBean = null;
    }

  }



  @Override
  public DetailsPmHandler<? extends PmDataInput> getDetailsPmHandler() {
    return detailsHandler;
  }
  public PmTable<?> getMasterTablePm() {
    return masterTablePm;
  }

  /**
   * @return The currently selected master record in case it is marked a actually changed within the details area. May be <code>null</code>.
   */
  private T_MASTER_BEAN getCurrentModifiedMasterRecord() {
    return detailsHandler.getDetailsPm().isPmValueChanged()
        ? getSelectedMasterBean()
        : null;
  }


}
