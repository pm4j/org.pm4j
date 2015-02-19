package org.pm4j.core.pm.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmTable.TableChange;
import org.pm4j.core.pm.api.PmEventApi;

/**
 * Observes {@link PageableCollection} events and calls the related {@link PmTableImpl} logic.
 *
 * @author Olaf Boede
 */
class InternalPmTableEventAdapterForPageableCollection {
  private static Logger LOG = LoggerFactory.getLogger(InternalPmTableEventAdapterForPageableCollection.class);

  private final PmTableImpl<?, ?> pmTable;
  private final PageableCollection<?> pageableCollection;

  /** Listens to {@link QueryParams} filter changes. Handles the table relates logic. */
  private final FilterChangeListener filterChangeListener;

  /** Listens to {@link QueryParams} sort order changes. Handles the table relates logic. */
  private final PropertyChangeListener sortOrderChangeListener;

  /** Listens for {@link PageableCollection} selection changes and handles the table relates logic. */
  private final SelectionChangeListener selectionChangeListener;


  public InternalPmTableEventAdapterForPageableCollection(final PmTableImpl<?, ?> pmTable, final PageableCollection<?> pageableCollection) {
    this.pmTable = pmTable;
    this.pageableCollection = pageableCollection;
    this.filterChangeListener = new FilterChangeListener();
    this.sortOrderChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        PmEventApi.firePmEvent(pmTable, PmEvent.SORT_ORDER_CHANGE);
      }
    };
    this.selectionChangeListener = new SelectionChangeListener();
  }

  /** Registers the needed listeners. */
  void registerListeners() {
    pageableCollection.getSelectionHandler().addPropertyAndVetoableListener(SelectionHandler.PROP_SELECTION, selectionChangeListener);
    pageableCollection.getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, sortOrderChangeListener);
    pageableCollection.getQueryParams().addPropertyAndVetoableListener(QueryParams.PROP_EFFECTIVE_FILTER, filterChangeListener);
  }

  /** Unregisters the listeners. Must be called to prevent memory leaks. */
  void unregisterListeners() {
    pageableCollection.getSelectionHandler().removePropertyAndVetoableListener(SelectionHandler.PROP_SELECTION, selectionChangeListener);
    pageableCollection.getQueryParams().removePropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, sortOrderChangeListener);
    pageableCollection.getQueryParams().removePropertyAndVetoableListener(QueryParams.PROP_EFFECTIVE_FILTER, filterChangeListener);
  }

  /**
   * A property change listener that forwards event calls to registered {@link TableChange#FILTER} decorators.
   * <p>
   * TODO olaf: should use an intermediate command to allow the standard logic for confirmed changes...
   */
  class FilterChangeListener implements PropertyAndVetoableChangeListener {

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      for (PmCommandDecorator d : pmTable.getPmDecorators(TableChange.FILTER)) {
        if (!d.beforeDo(null)) {
          String msg = "Decorator prevents filter change: " + d;
          LOG.debug(msg);
          throw new PropertyVetoException(msg, evt);
        }
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      pmTable.masterRowPm = null;
      // FIXME: may fire too often a DB query. What happens in case of a series of QueryParam changes?
      // PageableCollectionUtil2.ensureCurrentPageInRange(getPmPageableCollection());

      for (PmCommandDecorator d : pmTable.getPmDecorators(TableChange.FILTER)) {
        d.afterDo(null);
      }
      PmEventApi.firePmEvent(pmTable, PmEvent.FILTER_CHANGE);
    }
  }

  /**
   * A property change listener that forwards event calls to registered {@link TableChange#SELECTION} decorators.
   * <p>
   * TODO olaf: should use an intermediate command to allow the standard logic for confirmed changes...
   */
  class SelectionChangeListener implements PropertyAndVetoableChangeListener {

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      // a command used for value change reporting.
      PmCommand cmd = new PmAspectChangeCommandImpl(pmTable, "selection", evt.getOldValue(), evt.getNewValue());
      for (PmCommandDecorator d : pmTable.getPmDecorators(TableChange.SELECTION)) {
        if (!d.beforeDo(cmd)) {
          String msg = "Decorator prevents selection change: " + d;
          LOG.debug(msg);
          throw new PropertyVetoException(msg, evt);
        }
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      pmTable.masterRowPm = null;
      for (PmCommandDecorator d : pmTable.getPmDecorators(TableChange.SELECTION)) {
        d.afterDo(null);
      }
    }
  }


}
