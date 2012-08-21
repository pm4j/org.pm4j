package org.pm4j.core.pm.impl.changehandler;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable.TableChange;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A details handler for tables with an associated details PM that displays additional information
 * for the currently selected master table row.
 * <br>
 * It supports the following functionality:
 * <ul>
 *  <li>Listens for master table 'selection change' events.</li>
 *  <li>Prevents the selection change if the details area is not valid.</li>
 *  <li>Registers details area changes. See {@link #isDetailsChangeRegistered()} and {@link #getChangedDetailBeans()}</li>
 *  <li>Resets the details change information when the master table sends a {@link PmEvent#VALUE_CHANGE}.</li>
 * </ul>
 *
 * @param <T_DETAILS_BEAN> Type of beans handled by the related details PM.
 *
 * @author olaf boede
 */
public class MasterTableDetailsBeanPmHandler<T_DETAILS_BEAN> implements DetailsPmHandler {

    private static final Log LOG = LogFactory.getLog(MasterTableDetailsBeanPmHandler.class);

    protected final PmTable<?> masterTablePm;
    protected final PmBean<T_DETAILS_BEAN> detailsPm;
    private Set<T_DETAILS_BEAN> changedDetailBeans = new HashSet<T_DETAILS_BEAN>();

    /**
     * @param masterTablePm
     *            The table to observe.
     * @param detailsPm
     *            The details PM that is used for details data entry.
     */
    public MasterTableDetailsBeanPmHandler(PmTable<?> masterTablePm, PmBean<T_DETAILS_BEAN> detailsPm) {
        this.masterTablePm = masterTablePm;
        this.detailsPm = detailsPm;
        configure();
    }

    protected void configure() {
      masterTablePm.addDecorator(makeTableSelectionChangeDecorator(), TableChange.SELECTION);
      PmEventApi.addPmEventListener(masterTablePm, PmEvent.VALUE_CHANGE, makeTableValueChangeListener());
    }

    @Override
    public boolean isDetailsChangeRegistered() {
        return (!getChangedDetailBeans().isEmpty()) ||
               detailsPm.isPmValueChanged();
    }

    @Override
    public Set<T_DETAILS_BEAN> getChangedDetailBeans() {
      T_DETAILS_BEAN currentDetailsBean = detailsPm.getPmBean();
      if (detailsPm.isPmValueChanged() && !changedDetailBeans.contains(currentDetailsBean)) {
        HashSet<T_DETAILS_BEAN> set = new HashSet<T_DETAILS_BEAN>(changedDetailBeans);
        set.add(currentDetailsBean);
        return set;
      }
      else {
        return changedDetailBeans;
      }
    }

    /**
     * Sets the handler to an 'unchanged' state.
     */
    public void resetRegisteredChanges() {
      if (LOG.isDebugEnabled() && isDetailsChangeRegistered()) {
        LOG.debug("Reset master-details changed state for " + PmUtil.getPmLogString(detailsPm));
      }
      changedDetailBeans.clear();
    }

    /**
     * This gets called after switching to another master table record.
     * Found details area changes are already handled.
     * <p>
     * The default implementation just takes the bean behind the selected table row
     * and assigns is to the {@link #detailsPm} using a <code>setPmBean</code> call.
     */
    @SuppressWarnings("unchecked")
    protected void setDetailsForSelectedRow() {
        PmBean<?> row = (PmBean<?>) masterTablePm.getSelectedRow();
        ((PmBeanBase<Object>) detailsPm).setPmBean(row.getPmBean());
    }

    /**
     * Provides a listener that resets the registered changes in case of a table value change
     * (other records to handle).
     *
     * @return The listener.
     */
    protected PmEventListener makeTableValueChangeListener() {
        return new PmEventListener() {
            @Override
            public void handleEvent(PmEvent event) {
                resetRegisteredChanges();
            }
        };
    }

    /**
     * A decorator that prevents a value change if the details area is not valid and sets the
     * new details area if the change was executed.<br>
     * It also registers the changes of the details area.
     *
     * @return The decorator.
     */
    protected PmCommandDecorator makeTableSelectionChangeDecorator() {
        return new MasterSelectionChangeListener();
    }

    /**
     * A decorator that prevents a value change if the details area is not valid and sets the
     * new details area if the change was executed.<br>
     * It also registers the changes of the details area.
     */
    protected class MasterSelectionChangeListener implements PmCommandDecorator {
      @Override
      public boolean beforeDo(PmCommand cmd) {
          boolean isDetailsPmValid = PmValidationApi.validateSubTree(detailsPm);
          return isDetailsPmValid;
      }

      @Override
      public void afterDo(PmCommand cmd) {
          updateDetailsChangedState();
          setDetailsForSelectedRow();
      }

      protected void updateDetailsChangedState() {
          T_DETAILS_BEAN detailsBean = detailsPm.getPmBean();
          // @formatter:off
          if (detailsBean != null &&
              !changedDetailBeans.contains(detailsBean) &&
              detailsPm.isPmValueChanged()) {

              changedDetailBeans.add(detailsBean);

              if (LOG.isDebugEnabled()) {
                  LOG.debug("Registered a master-details change for " + PmUtil.getPmLogString(detailsPm));
              }
          }
          // @formatter:on
      }
    }

}
