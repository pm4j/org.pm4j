package org.pm4j.core.pm.impl.pageable;

import java.util.Iterator;

import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmFactoryApi;

/**
 * A non serializeable selection that provides PMs for a backing selection of beans.
 */
@SuppressWarnings("serial")
public class PmBeanSelection<T_PM extends PmBean<T_BEAN>, T_BEAN> implements Selection<T_PM> {

  private final PmObject factoryCtxtPm;
  final Selection<T_BEAN> beanSelection;

  public PmBeanSelection(PmObject factoryCtxtPm, Selection<T_BEAN> selectedBeans) {
    assert factoryCtxtPm != null;
    assert selectedBeans != null;

    this.factoryCtxtPm = factoryCtxtPm;
    this.beanSelection = selectedBeans;
  }

  @Override
  public long getSize() {
    return beanSelection.getSize();
  }

  @Override
  public boolean isEmpty() {
    return beanSelection.isEmpty();
  }

  @Override
  public Iterator<T_PM> iterator() {
    return new Iterator<T_PM>() {
      private Iterator<T_BEAN> beanIterator = beanSelection.iterator();

      @Override
      public boolean hasNext() {
        return beanIterator.hasNext();
      }

      @Override
      public T_PM next() {
        T_BEAN b = beanIterator.next();
        T_PM pm = PmFactoryApi.getPmForBean(factoryCtxtPm, b);
        return pm;
      }

      @Override
      public void remove() {
        throw new PmRuntimeException("This operation is not supported. Please SelectionHander.select(false) instead.");
      }
    };
  }

  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    beanSelection.setIteratorBlockSizeHint(readBlockSize);
  }

  @Override
  public boolean contains(T_PM item) {
    return item != null
        ? beanSelection.contains(item.getPmBean())
        : false;
  }

  public Selection<T_BEAN> getBeanSelection() {
    return beanSelection;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean hasSameItemSet(Selection<T_PM> other) {
    if (!(other instanceof PmBeanSelection)) {
      throw new UnsupportedOperationException("Unable to compare to: " + other);
    }
    return SelectionHandlerUtil.sameSelection(beanSelection, ((PmBeanSelection<T_PM, T_BEAN>)other).beanSelection);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(beanSelection=" + beanSelection.toString() + ")";
  }

}