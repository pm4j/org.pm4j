package org.pm4j.core.pm.impl.pageable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;

import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.util.beanproperty.PropertyAndVetoableChangeListener;
import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;

/**
 * A {@link SelectionHandler} for a set of {@link PmBean}s.
 * <p>
 * It is baked by another {@link SelectionHandler} that handles the selection using the beans behind
 * item PMs.<br>
 * It is useful for UI components that have to present only a subset of the collection as a PM.<br>
 * E.g. a pageable table that needs to have PMs only for the rows on the current page.<br>
 * In such cases the internal {@link #baseSelectionHandler} may handle the selection more efficient
 * by holding only the beans or the identifiers in memory.
 *
 * @param <T_PM> the type of handled {@link PmBean} items.
 * @param <T_BEAN> the type of beans behind {@link PmBean}s.
 *
 * @author olaf boede
 */
// TODO olaf: Should the selection change event be generated here? Decide and implement...
public class PmBeanSelectionHandler<T_PM extends PmBean<T_BEAN>, T_BEAN> extends PropertyChangeSupportedBase implements SelectionHandler<T_PM> {

  private final SelectionHandler<T_BEAN> baseSelectionHandler;
  private final PmObject factoryCtxtPm;

  @SuppressWarnings("unchecked")
  public PmBeanSelectionHandler(PmObject factoryCtxtPm, SelectionHandler<? extends T_BEAN> baseSelector) {
    assert factoryCtxtPm != null;
    assert baseSelector != null;

    this.baseSelectionHandler = (SelectionHandler<T_BEAN>) baseSelector;
    this.factoryCtxtPm = factoryCtxtPm;

    // forward all events from the baseSelectionHandler as own events.
    baseSelectionHandler.addPropertyAndVetoableListener(PROP_SELECTION, new ForwardAsPmSelectionChangeEventListener());
  }

  @Override
  public SelectMode getSelectMode() {
    return baseSelectionHandler.getSelectMode();
  }

  @Override
  public void setSelectMode(SelectMode selectMode) {
    baseSelectionHandler.setSelectMode(selectMode);
  }

  @Override
  public boolean select(boolean select, T_PM item) {
     return SelectionHandlerUtil.selectInSameForceMode(this, baseSelectionHandler, select, item.getPmBean());
  }

  @Override
  public boolean select(boolean select, Iterable<T_PM> items) {
    assert items != null;

    Collection<T_BEAN> beans = new ArrayList<T_BEAN>();
    for (T_PM i : items) {
      beans.add(i.getPmBean());
    }

    return SelectionHandlerUtil.selectInSameForceMode(this, baseSelectionHandler, select, beans);
  }

  @Override
  public boolean selectAll(boolean select) {
    return SelectionHandlerUtil.selectAllInSameForceMode(this, baseSelectionHandler, select);
  }

  @Override
  public boolean invertSelection() {
    return SelectionHandlerUtil.invertSelectionInSameForceMode(this, baseSelectionHandler);
  }

  @Override
  public Selection<T_PM> getSelection() {
    return new PmBeanSelection<T_PM, T_BEAN>(factoryCtxtPm, baseSelectionHandler.getSelection());
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean setSelection(Selection<T_PM> selection) {
    Selection<T_BEAN> beanSelection = ((PmBeanSelection<T_PM, T_BEAN>)selection).beanSelection;
    return SelectionHandlerUtil.setSelectionInSameForceMode(this, baseSelectionHandler, beanSelection);
  }

  @Override
  public Selection<T_PM> getAllItemsSelection() {
    return new PmBeanSelection<T_PM, T_BEAN>(factoryCtxtPm, baseSelectionHandler.getAllItemsSelection());
  }

  @Override
  public void addSelectionHandlerCallback(SelectionHandlerCallback callback) {
    baseSelectionHandler.addSelectionHandlerCallback(callback);
  }

  @Override
  public void ensureSelectionStateRequired() {
    baseSelectionHandler.ensureSelectionStateRequired();
  }

  /**
   * Provides the selection handler for the beans behind the item PM's.
   *
   * @return the bean selection handler.
   */
  public SelectionHandler<T_BEAN> getBeanSelectionHandler() {
    return baseSelectionHandler;
  }

  /** Forwards the received events to own listeners. */
  private class ForwardAsPmSelectionChangeEventListener implements PropertyAndVetoableChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      firePropertyChange(makePmSelectionChangeEvent(evt));
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      fireVetoableChange(makePmSelectionChangeEvent(evt));
    }

    @SuppressWarnings("unchecked")
    private PropertyChangeEvent makePmSelectionChangeEvent(PropertyChangeEvent baseSelectionEvent) {
      PropertyChangeEvent newEvent = new PropertyChangeEvent(
            this,
            baseSelectionEvent.getPropertyName(),
            new PmBeanSelection<T_PM, T_BEAN>(factoryCtxtPm, (Selection<T_BEAN>)baseSelectionEvent.getOldValue()),
            new PmBeanSelection<T_PM, T_BEAN>(factoryCtxtPm, (Selection<T_BEAN>)baseSelectionEvent.getNewValue()));
      newEvent.setPropagationId(baseSelectionEvent.getPropagationId());
      return newEvent;
    }

  }

}
