package org.pm4j.core.pm.impl.changehandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.selection.ItemSetSelection;
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
    Validate.notNull(masterTablePm, "masterTablePm should not be null");

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
        Set<T_DETAILS_BEAN> affectedDetails = new HashSet<T_DETAILS_BEAN>();
        // Iterate over the modifications and not over the selection.
        // Selection iteration may be slow but it's contains() method is fast.
        for (T_MASTER_BEAN b : new ArrayList<T_MASTER_BEAN>(getMasterBeanToDetailsModificationsMap().keySet())) {
          if (deletedMasterSelection.contains(b)) {
            Modifications<T_DETAILS_BEAN> m = removeMasterBeanModifications(b);
            if (m != null) {
              affectedDetails.addAll(m.getAddedItems());
              affectedDetails.addAll(m.getUpdatedItems());
            }
          }
        }

        // 2. Inform details table listeners about the deletion of the details selection as well.
        //    The dependent details records disappear with the removed master record.
        if (!affectedDetails.isEmpty()) {
          PropertyChangeSupportedBase pc = (PropertyChangeSupportedBase) getDetailsTable().getPmPageableBeanCollection();
          pc.firePropertyChange(PageableCollection.EVENT_REMOVE_SELECTION, new ItemSetSelection<T_DETAILS_BEAN>(affectedDetails), null);
        }
      }

    });
  }

  @Override
  protected boolean shouldProcessMasterChange(Object oldMasterBean, Object newMasterBean) {
    if (oldMasterBean != newMasterBean)
      return true;

    // special execution condition that was introduced to handle delete of the last master record.
    return masterTablePm.getTotalNumOfPmRows() == 0;
  }
}
