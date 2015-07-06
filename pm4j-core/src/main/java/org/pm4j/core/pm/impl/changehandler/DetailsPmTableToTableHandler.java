package org.pm4j.core.pm.impl.changehandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.selection.Selection;
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
        @SuppressWarnings("unchecked")
        Selection<T_MASTER_BEAN> deletedSelection = (Selection<T_MASTER_BEAN>) evt.getOldValue();
        // Iterate over the modifications and not over the selection.
        // Selection iteration may be slow but it's contains() method is fast.
        for (T_MASTER_BEAN b : new ArrayList<T_MASTER_BEAN>(getMasterBeanToDetailsModificationsMap().keySet())) {
          if (deletedSelection.contains(b)) {
            removeMasterBeanModifications(b);
          }
        }
      }
    });
  }

}
