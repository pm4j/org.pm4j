package org.pm4j.core.pm.impl.changehandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;
import org.pm4j.core.pm.impl.PmTableImpl;

/**
 * A details change handler for the combination of a master table and a details table.
 *
 * @author Olaf Boede
 *
 * @param <T_MASTER_BEAN> The master bean type.
 * @param <T_DETAILS_BEAN> The details bean type.
 */
public class DetailsPmTableToTableHandler<T_MASTER_BEAN, T_DETAILS_BEAN> extends DetailsPmTableHandlerBase<T_MASTER_BEAN, T_DETAILS_BEAN>{

  private final PmTableImpl<?, ? extends T_MASTER_BEAN> masterTablePm;

  public DetailsPmTableToTableHandler(PmTableImpl<?, ? extends T_MASTER_BEAN> masterTablePm, PmTableImpl<?, ? extends T_DETAILS_BEAN> detailsTablePm) {
    super(detailsTablePm);
    assert masterTablePm != null : "masterTablePm should not be null";

    this.masterTablePm = masterTablePm;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Modifications<T_MASTER_BEAN> getMasterModifications() {
    return (Modifications<T_MASTER_BEAN>) masterTablePm.getPmPageableBeanCollection().getModifications();
  }

  @Override
  public void startObservers() {
    super.startObservers();

    // Whenever a master record gets deleteted, its corresponding details changes are no longer relevant
    // and have to be forgotten.
    masterTablePm.getPmPageableBeanCollection().addPropertyChangeListener(PageableCollection.EVENT_REMOVE_SELECTION, new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {

        // 1. Remove all modification entries related to the deleted master.
        @SuppressWarnings("unchecked")
        Selection<T_MASTER_BEAN> deletedMasterSelection = (Selection<T_MASTER_BEAN>) evt.getOldValue();
        // Iterate over the modifications and not over the selection.
        // Selection iteration may be slow but it's contains() method is fast.
        for (T_MASTER_BEAN b : new ArrayList<T_MASTER_BEAN>(getMasterBeanToDetailsModificationsMap().keySet())) {
          if (deletedMasterSelection.contains(b)) {
            removeMasterBeanModifications(b);
          }
        }

        // 2. Inform details table listeners about the deletion of the details selection as well.
        //    The dependent details records disappear with the removed master record.
        Selection<T_DETAILS_BEAN> detailsSelection = getDetailsTable().getPmPageableBeanCollection().getSelection();
        if (!detailsSelection.isEmpty()) {
          PropertyChangeSupportedBase pc = (PropertyChangeSupportedBase) getDetailsTable().getPmPageableBeanCollection();
          pc.firePropertyChange(new PropertyChangeEvent(pc, PageableCollection.EVENT_REMOVE_SELECTION, detailsSelection, null));
        }
      }

    });
  }

  @Override
  protected boolean shouldProcessMasterChange(Object oldMasterBean, Object newMasterBean) {
    if (oldMasterBean != newMasterBean)
      return true;

    return masterTablePm.getTotalNumOfPmRows() == 0;
  }
}
